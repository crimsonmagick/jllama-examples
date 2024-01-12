package net.jllama.examples.chat.infrastructure;

import static net.jllama.examples.chat.infrastructure.ModelInfoService.ModelType.LLAMA_2;

import lombok.RequiredArgsConstructor;
import net.jllama.api.Model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModelInfoService {

  private static final int LLAMA_MAX_INPUT_TOKENS = 3000;
  @Value("${prompts.initial.chat}")
  private final String initialPrompt;
  private final Model llamaApiModel;

  public enum ModelType {
    LLAMA_2("LLAMA_2");

    private final String modelString;

    ModelType(final String modelString) {
      this.modelString = modelString;
    }

    public String modelString() {
      return modelString;
    }

    public static ModelType fromString(final String modelName) {
      if (modelName.startsWith("llama")) {
        return LLAMA_2;
      }
      throw new RuntimeException("Unrecognized model type.");
    }
  }

  public Tokenizer getTokenizer(final ModelType modelType) {
    if (modelType == LLAMA_2) {
      return new LlamaTokenizer(llamaApiModel);
    }
    throw new RuntimeException("Unrecognized model type.");
  }

  public String getInitialPrompt(final ModelType model) {
    if (model == LLAMA_2) {
      return initialPrompt;
    }
    throw new RuntimeException("Unrecognized model type.");
  }

  public int getMaxInputTokens(final ModelType model) {
    if (model == LLAMA_2) {
      return LLAMA_MAX_INPUT_TOKENS;
    }
    throw new RuntimeException("Unrecognized model type.");
  }

}
