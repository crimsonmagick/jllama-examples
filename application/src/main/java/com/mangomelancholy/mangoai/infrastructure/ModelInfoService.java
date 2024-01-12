package com.mangomelancholy.mangoai.infrastructure;

import static com.mangomelancholy.mangoai.infrastructure.ModelInfoService.ModelType.DAVINCI;
import static com.mangomelancholy.mangoai.infrastructure.ModelInfoService.ModelType.GPT_3_5;
import static com.mangomelancholy.mangoai.infrastructure.ModelInfoService.ModelType.GPT_4;
import static com.mangomelancholy.mangoai.infrastructure.ModelInfoService.ModelType.LLAMA_2;

import lombok.RequiredArgsConstructor;
import net.jllama.api.Model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModelInfoService {

  // TODO put in config file, map to model types... or replace with something dynamic
  private static final int GPT4_MAX_INPUT_TOKENS = 7000;
  private static final int GPT4_MAX_TOKENS = 8192;
  private static final int GPT3_MAX_INPUT_TOKENS = 3000;
  private static final int GPT3_MAX_TOKENS = 4097;
  private static final int LLAMA_MAX_INPUT_TOKENS = 3000;
  private static final int LLAMA_MAX_TOKENS = 4096;
  @Value("${seeds.chat.gpt.conversation}")
  private final String chatGptSeed;
  @Value("${seeds.davinci.conversation}")
  private final String davinciSeed;
  private final Model llamaApiModel;

  public enum ModelType {
    GPT_3_5("gpt-3.5-turbo"), GPT_4("gpt-4"), DAVINCI("davinci"), LLAMA_2("LLAMA_2");

    private final String modelString;

    ModelType(final String modelString) {
      this.modelString = modelString;
    }

    public static ModelType fromString(final String modelName) {
      if (modelName.startsWith("gpt-3")) {
        return GPT_3_5;
      } else if (modelName.startsWith("gpt-4")) {
        return GPT_4;
      } else if (modelName.startsWith("davinci")) {
        return DAVINCI;
      } else if (modelName.startsWith("llama")) {
        return LLAMA_2;
      }
      throw new RuntimeException("Unrecognized model type.");
    }

    public String modelString() {
      return modelString;
    }
  }

  public Tokenizer getTokenizer(final ModelType modelType) {
    if (modelType == LLAMA_2) {
      return new LlamaTokenizer(llamaApiModel);
    } else if (modelType == GPT_3_5 || modelType == GPT_4 || modelType == DAVINCI) {
      return new GptTokenizer(modelType);
    }
    throw new RuntimeException("Unrecognized model type.");
  }

  public String getInitialPrompt(final ModelType model) {
    if (model == DAVINCI) {
      return davinciSeed;
    }
    if (model == GPT_3_5 || model == GPT_4 || model == LLAMA_2) {
      return chatGptSeed;
    }
    throw new RuntimeException("Unrecognized model type.");
  }

  public int getMaxInputTokens(final ModelType model) {
    if ((model == DAVINCI) || (model == GPT_3_5)) {
      return GPT3_MAX_INPUT_TOKENS;
    } else if (model == GPT_4) {
      return GPT4_MAX_INPUT_TOKENS;
    } else if (model == LLAMA_2) {
      return LLAMA_MAX_INPUT_TOKENS;
    }
    throw new RuntimeException("Unrecognized model type.");
  }

  public int getMaxResponseTokens(final ModelType model) {
    if (model == DAVINCI || model == GPT_3_5) {
      return GPT3_MAX_TOKENS - GPT3_MAX_INPUT_TOKENS;
    } else if (model == GPT_4) {
      return GPT4_MAX_TOKENS - GPT4_MAX_INPUT_TOKENS;
    } else if (model == LLAMA_2) {
      return LLAMA_MAX_TOKENS - LLAMA_MAX_INPUT_TOKENS;
    }
    throw new RuntimeException("Unrecognized model type.");
  }

  public int getMessagePaddingTokens(final ModelType model) {
    if (model == DAVINCI || model == LLAMA_2) {
      return 0;
    } else if ((model == GPT_4) || (model == GPT_3_5)) {
      return 6;
    }
    throw new RuntimeException("Unrecognized model type.");
  }

}
