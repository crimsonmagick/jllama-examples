package net.jllama.examples.chat.adapters.outbound.memory;

import lombok.RequiredArgsConstructor;
import net.jllama.api.Model;
import net.jllama.examples.chat.application.conversation.ConversationEntity;
import net.jllama.examples.chat.application.conversation.ExpressionValue;
import net.jllama.examples.chat.application.conversation.ExpressionValue.ActorType;
import net.jllama.examples.chat.application.conversation.ports.secondary.MemoryService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@RequiredArgsConstructor
@Service
public class WindowedMemoryService implements MemoryService {

  final Model model;

  @Override
  public ConversationEntity rememberConversation(final ConversationEntity conversation) {
    final long MAX_INPUT_TOKENS = 3000;
    final List<Tuple2<Long, ExpressionValue>> tokenPairs = conversation.getExpressions().stream()
        .map(value -> {
          final long tokenCount = model.tokens().tokenize(value.content()).size();
          return Tuples.of(tokenCount, value);
        })
        .toList();
    long totalTokens = tokenPairs.stream()
        .map(Tuple2::getT1)
        .reduce(0L, Long::sum);
    final List<ExpressionValue> remembered;
    if (totalTokens > MAX_INPUT_TOKENS) {
      remembered = new ArrayList<>();
      for (final Tuple2<Long, ExpressionValue> pair : tokenPairs) {
        if (totalTokens <= MAX_INPUT_TOKENS || pair.getT2().actor() == ActorType.SYSTEM) {
          remembered.add(pair.getT2());
        } else {
          totalTokens = totalTokens - pair.getT1();
        }
      }
    } else {
      remembered = new ArrayList<>(conversation.getExpressions());
    }
    return new ConversationEntity(conversation.getConversationId(), remembered,
        conversation.getSummary());
  }
}
