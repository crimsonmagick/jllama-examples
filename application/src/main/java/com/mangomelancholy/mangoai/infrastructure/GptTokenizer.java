package com.mangomelancholy.mangoai.infrastructure;

import static com.mangomelancholy.mangoai.infrastructure.ModelInfoService.ModelType.DAVINCI;
import static com.mangomelancholy.mangoai.infrastructure.ModelInfoService.ModelType.GPT_3_5;
import static com.mangomelancholy.mangoai.infrastructure.ModelInfoService.ModelType.GPT_4;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import com.mangomelancholy.mangoai.infrastructure.ModelInfoService.ModelType;
import java.util.List;

public class GptTokenizer implements Tokenizer {

  private final Encoding encoding;
  private final static EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();

  public GptTokenizer(final ModelType modelType) {
    encoding = registry.getEncoding(getEncoding(modelType));
  }

  private EncodingType getEncoding(final ModelType modelType) {
    if (modelType == DAVINCI) {
      return EncodingType.R50K_BASE;
    }
    if (modelType == GPT_3_5 || modelType == GPT_4) {
      return EncodingType.CL100K_BASE;
    }
    throw new RuntimeException("Unrecognized model type.");
  }

  @Override
  public List<Integer> tokenize(final String text) {
    return encoding.encode(text);
  }

  @Override
  public int countTokens(String text) {
    return encoding.encode(text).size();
  }
}
