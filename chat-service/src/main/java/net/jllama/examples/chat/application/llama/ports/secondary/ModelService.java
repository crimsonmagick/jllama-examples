package net.jllama.examples.chat.application.llama.ports.secondary;

import java.util.List;
import reactor.core.publisher.Mono;

public interface ModelService {

  Mono<Integer> getBos();

  Mono<Integer> getEos();

  Mono<Integer> getNl();
  Mono<List<Integer>> tokenize(String text, boolean addBos, boolean enableControlCharacters);
  Mono<String> detokenize(List<Integer> tokens);

}
