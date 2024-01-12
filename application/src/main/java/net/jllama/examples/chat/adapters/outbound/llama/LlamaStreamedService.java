package net.jllama.examples.chat.adapters.outbound.llama;

import net.jllama.examples.chat.application.conversation.ConversationEntity;
import net.jllama.examples.chat.application.conversation.ExpressionFragment;
import net.jllama.examples.chat.application.conversation.ports.secondary.AiStreamedService;
import net.jllama.examples.chat.infrastructure.ModelInfoService.ModelType;
import net.jllama.examples.chat.infrastructure.llama2.chat.LlamaService;
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
