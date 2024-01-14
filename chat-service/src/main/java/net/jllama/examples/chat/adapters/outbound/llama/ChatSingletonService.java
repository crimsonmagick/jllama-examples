package net.jllama.examples.chat.adapters.outbound.llama;

import static net.jllama.examples.chat.application.conversation.ConversationEntity.fromRecord;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.jllama.examples.chat.application.conversation.ConversationEntity;
import net.jllama.examples.chat.application.conversation.ExpressionValue;
import net.jllama.examples.chat.application.conversation.ExpressionValue.ActorType;
import net.jllama.examples.chat.application.conversation.ports.secondary.AiSingletonService;
import net.jllama.examples.chat.application.conversation.ports.secondary.ConversationRepository;
import net.jllama.examples.chat.infrastructure.llama2.chat.LlamaService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class ChatSingletonService implements AiSingletonService {

  private final LlamaService llamaService;
  private final LlamaSerializer serializer;
  private final ConversationRepository conversationRepository;

  @Override
  public Mono<ExpressionValue> exchange(final ConversationEntity conversationEntity) {
    final String conversationId = conversationEntity.getConversationId();
    if (llamaService.isCached(conversationId)) {
      throw new IllegalArgumentException("Cannot exchange a whole conversation if the conversation "
          + "has been previously exchanged.");
    }
    return llamaService.complete(conversationId, serializer.serialize(conversationEntity))
        .collect(Collectors.joining())
        .map(response -> new ExpressionValue(response, ActorType.AGENT, conversationId));
  }

  @Override
  public Mono<ExpressionValue> exchange(final ExpressionValue expressionValue) {
    final String conversationId = expressionValue.conversationId();
    final Mono<String> toCompleteMono;
    // TODO make sure synchronization is done on a per conversation basis up stream. Use a db write row lock on the conversation.
    if (llamaService.isCached(conversationId)) {
      toCompleteMono = Mono.just(serializer.serialize(expressionValue));
    } else {
      toCompleteMono = conversationRepository.getConversation(conversationId)
          .map(record -> serializer.serialize(fromRecord(record)));
    }
    return toCompleteMono.flatMap(toComplete ->
        llamaService.complete(conversationId, serializer.serialize(expressionValue))
            .collect(Collectors.joining())
            .map(response -> new ExpressionValue(response, ActorType.AGENT, conversationId)));
  }
}