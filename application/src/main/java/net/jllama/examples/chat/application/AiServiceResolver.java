package net.jllama.examples.chat.application;

import net.jllama.examples.chat.adapters.outbound.llama.Llama2ChatSingletonService;
import net.jllama.examples.chat.adapters.outbound.llama.Llama2ChatStreamedService;
import net.jllama.examples.chat.application.conversation.ports.secondary.AiSingletonService;
import net.jllama.examples.chat.application.conversation.ports.secondary.AiStreamedService;
import net.jllama.examples.chat.infrastructure.ModelInfoService.ModelType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AiServiceResolver {

  private final Llama2ChatSingletonService llama2ChatSingletonService;
  private final Llama2ChatStreamedService llama2ChatStreamedService;

  public AiSingletonService resolveSingletonService(final ModelType modelType) {
    return llama2ChatSingletonService;
  }

  public AiStreamedService resolveStreamedService(final ModelType modelType) {
    return llama2ChatStreamedService;
  }

}
