package net.jllama.examples.chat.adapters.outbound.llama;

import static net.jllama.examples.chat.application.conversation.ConversationEntity.fromRecord;

import lombok.RequiredArgsConstructor;
import net.jllama.examples.chat.application.conversation.ConversationEntity;
import net.jllama.examples.chat.application.conversation.ExpressionFragment;
import net.jllama.examples.chat.application.conversation.ExpressionValue;
import net.jllama.examples.chat.application.conversation.ports.secondary.AiStreamedService;
import net.jllama.examples.chat.application.conversation.ports.secondary.ConversationRepository;
import net.jllama.examples.chat.infrastructure.llama2.chat.LlamaChatService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class ChatStreamedService implements AiStreamedService {

  private final ConversationRepository conversationRepository;
  private final LlamaChatService llamaChatService;
  private final LlamaSerializer serializer;

  @Override
  public Flux<ExpressionFragment> exchange(final ConversationEntity conversationEntity) {
    final String conversationId = conversationEntity.getConversationId();
    if (llamaChatService.isCached(conversationId)) {
      throw new IllegalArgumentException("Cannot exchange a whole conversation if the conversation "
          + "has been previously exchanged.");
    }
    return llamaChatService.complete(conversationId, serializer.serialize(conversationEntity))
        .index()
        .map(respTuple -> new ExpressionFragment(respTuple.getT2(), conversationId,
            respTuple.getT1()));
  }

  @Override
  public Flux<ExpressionFragment> exchange(ExpressionValue expressionValue) {
    final String conversationId = expressionValue.conversationId();
    final Mono<String> toCompleteMono;
    // TODO make sure synchronization is done on a per conversation basis up stream. Use a db write row lock on the conversation.
    if (llamaChatService.isCached(conversationId)) {
      toCompleteMono = Mono.just(serializer.serialize(expressionValue));
    } else {
      toCompleteMono = conversationRepository.getConversation(conversationId)
          .map(record -> serializer.serialize(fromRecord(record)));
    }
    return toCompleteMono.flatMapMany(toComplete ->
            llamaChatService.complete(conversationId, toComplete))
        .index()
        .map(respTuple -> new ExpressionFragment(respTuple.getT2(), conversationId,
            respTuple.getT1()));
  }

}
