package net.jllama.examples.chat.adapters.outbound.memory;

import net.jllama.examples.chat.application.conversation.ConversationEntity;
import net.jllama.examples.chat.application.conversation.ExpressionValue;
import net.jllama.examples.chat.application.conversation.ExpressionValue.ActorType;
import net.jllama.examples.chat.application.conversation.ports.secondary.MemoryService;
import net.jllama.examples.chat.infrastructure.ModelInfoService;
import net.jllama.examples.chat.infrastructure.ModelInfoService.ModelType;
import net.jllama.examples.chat.infrastructure.Tokenizer;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Service
public class WindowedMemoryService implements MemoryService {

  private final ModelInfoService modelInfoService;

  WindowedMemoryService(final ModelInfoService modelInfoService) {
    this.modelInfoService = modelInfoService;
  }

  @Override
  public ConversationEntity rememberConversation(final ConversationEntity conversation,
      String model) {
    final ModelType modelType = ModelType.fromString(model);
    final long MAX_INPUT_TOKENS = modelInfoService.getMaxInputTokens(modelType);
    final Tokenizer tokenizer = modelInfoService.getTokenizer(modelType);
    final List<Tuple2<Long, ExpressionValue>> tokenPairs = conversation.getExpressions().stream()
        .map(value -> {
          final long tokenCount = tokenizer.countTokens(value.content());
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
        if (totalTokens <= MAX_INPUT_TOKENS || pair.getT2().actor() == ActorType.INITIAL_PROMPT) {
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