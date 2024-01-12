package net.jllama.examples.chat.infrastructure;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.jllama.api.Model;

@RequiredArgsConstructor
public class LlamaTokenizer implements Tokenizer {

  private final Model model;

  @Override
  public List<Integer> tokenize(String text) {
    return model.tokens().tokenize(text, false, false);
  }

  @Override
  public int countTokens(String text) {
    return model.tokens().tokenize(text, false, true).size();
  }
}
