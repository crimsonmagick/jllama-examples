package com.mangomelancholy.mangoai.application;

import com.mangomelancholy.mangoai.adapters.outbound.llama.LlamaSingletonService;
import com.mangomelancholy.mangoai.adapters.outbound.llama.LlamaStreamedService;
import com.mangomelancholy.mangoai.application.conversation.ports.secondary.AiSingletonService;
import com.mangomelancholy.mangoai.application.conversation.ports.secondary.AiStreamedService;
import com.mangomelancholy.mangoai.infrastructure.ModelInfoService.ModelType;
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
