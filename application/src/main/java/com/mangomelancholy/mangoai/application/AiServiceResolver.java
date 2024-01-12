package com.mangomelancholy.mangoai.application;

import com.mangomelancholy.mangoai.adapters.outbound.chat.GptChatSingletonService;
import com.mangomelancholy.mangoai.adapters.outbound.chat.GptChatStreamedService;
import com.mangomelancholy.mangoai.adapters.outbound.davinci.DavinciSingletonService;
import com.mangomelancholy.mangoai.adapters.outbound.davinci.DavinciStreamedService;
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

  private final DavinciSingletonService davinciSingletonService;
  private final DavinciStreamedService davinciStreamedService;
  private final GptChatSingletonService gptChatSingletonService;
  private final GptChatStreamedService gptChatStreamedService;
  private final LlamaSingletonService llamaSingletonService;
  private final LlamaStreamedService llamaStreamedService;

  public AiSingletonService resolveSingletonService(final ModelType modelType) {
    return switch (modelType) {
      case GPT_4, GPT_3_5 -> gptChatSingletonService;
      case LLAMA_2 -> llamaSingletonService;
      default -> davinciSingletonService;
    };
  }

  public AiStreamedService resolveStreamedService(final ModelType modelType) {
    return switch (modelType) {
      case GPT_4, GPT_3_5 -> gptChatStreamedService;
      case LLAMA_2 -> llamaStreamedService;
      default -> davinciStreamedService;
    };
  }

}
