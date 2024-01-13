package net.jllama.examples.chat.adapters.outbound.llama;

import net.jllama.examples.chat.application.conversation.ConversationEntity;
import net.jllama.examples.chat.application.conversation.ExpressionValue;
import net.jllama.examples.chat.application.conversation.ExpressionValue.ActorType;
import net.jllama.examples.chat.application.conversation.ports.secondary.AiSingletonService;
import net.jllama.examples.chat.infrastructure.ModelInfoService.ModelType;
import net.jllama.examples.chat.infrastructure.llama2.chat.LlamaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class Llama2ChatSingletonService implements AiSingletonService {

  private final LlamaService llamaService;
  private final LlamaConversationSerializer serializer;

  @Override
  public Mono<ExpressionValue> exchange(final ConversationEntity conversationEntity,
      final ModelType modelType) {
    return llamaService.singletonCompletion(serializer.serialize(conversationEntity))
        .map(response -> new ExpressionValue(response, ActorType.PAL,
            conversationEntity.getConversationId()));
  }
}