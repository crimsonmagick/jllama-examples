package com.mangomelancholy.mangoai.infrastructure;

import java.util.List;

public interface Tokenizer {
  List<Integer> tokenize(final String text);
  int countTokens(final String text);

}
