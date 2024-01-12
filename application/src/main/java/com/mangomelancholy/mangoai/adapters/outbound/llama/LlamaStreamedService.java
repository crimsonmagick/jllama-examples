package com.mangomelancholy.mangoai.adapters.outbound.llama;

import com.mangomelancholy.mangoai.application.conversation.ConversationEntity;
import com.mangomelancholy.mangoai.application.conversation.ExpressionFragment;
import com.mangomelancholy.mangoai.application.conversation.ExpressionValue;
import com.mangomelancholy.mangoai.application.conversation.ExpressionValue.ActorType;
import com.mangomelancholy.mangoai.application.conversation.ports.secondary.AiStreamedService;
import com.mangomelancholy.mangoai.infrastructure.ModelInfoService.ModelType;
import com.mangomelancholy.mangoai.infrastructure.llama2.chat.LlamaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Service
public class LlamaStreamedService implements AiStreamedService {

  private final LlamaService llamaService;
  private final LlamaConversationSerializer serializer;

  @Override
  public Flux<ExpressionFragment> exchange(final ConversationEntity conversationEntity,
      ModelType modelType) {
    return llamaService.streamCompletion(serializer.serialize(conversationEntity))
        .index()
        .map(response -> new ExpressionFragment(response.getT2(),
            conversationEntity.getConversationId(), response.getT1()));
  }
}
