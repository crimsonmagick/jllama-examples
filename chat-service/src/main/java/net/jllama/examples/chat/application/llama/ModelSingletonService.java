package net.jllama.examples.chat.application.llama;

import java.util.List;
import reactor.core.publisher.Mono;

public interface ModelSingletonService {

  Mono<SpecialTokens> getSpecialTokens();

  Mono<List<Integer>> tokenize(String text, boolean addBos, boolean enableControlCharacters);

  Mono<String> detokenize(List<Integer> tokens);
}
