package net.jllama.examples.chat.infrastructure;

import java.util.List;

public interface Tokenizer {
  List<Integer> tokenize(final String text);
  int countTokens(final String text);

}
