package com.mangomelancholy.mangoai.adapters.outbound.llama;

import com.mangomelancholy.mangoai.application.conversation.ConversationEntity;
import com.mangomelancholy.mangoai.application.conversation.ExpressionValue;
import com.mangomelancholy.mangoai.application.conversation.ExpressionValue.ActorType;
import com.mangomelancholy.mangoai.application.conversation.ports.secondary.AiSingletonService;
import com.mangomelancholy.mangoai.infrastructure.ModelInfoService.ModelType;
import com.mangomelancholy.mangoai.infrastructure.llama2.chat.LlamaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class LlamaSingletonService implements AiSingletonService {

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
