package net.jllama.examples.chat.application;

import net.jllama.examples.chat.adapters.outbound.llama.LlamaSingletonService;
import net.jllama.examples.chat.adapters.outbound.llama.LlamaStreamedService;
import net.jllama.examples.chat.application.conversation.ports.secondary.AiSingletonService;
import net.jllama.examples.chat.application.conversation.ports.secondary.AiStreamedService;
import net.jllama.examples.chat.infrastructure.ModelInfoService.ModelType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AiServiceResolver {

  private final LlamaSingletonService llamaSingletonService;
  private final LlamaStreamedService llamaStreamedService;

  public AiSingletonService resolveSingletonService(final ModelType modelType) {
    return llamaSingletonService;
  }

  public AiStreamedService resolveStreamedService(final ModelType modelType) {
    return llamaStreamedService;
  }

}
