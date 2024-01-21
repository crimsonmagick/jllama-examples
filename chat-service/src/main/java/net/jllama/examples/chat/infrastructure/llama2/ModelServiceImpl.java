package net.jllama.examples.chat.infrastructure.llama2;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.jllama.api.Model;
import net.jllama.examples.chat.application.llama.ports.secondary.ModelService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class ModelServiceImpl implements ModelService {

  private final Model model;

  @Override
  public Mono<Integer> getBos() {
    return Mono.just(model.tokens().bos());
  }

  @Override
  public Mono<Integer> getEos() {
    return Mono.just(model.tokens().eos());
  }

  @Override
  public Mono<Integer> getNl() {
    return Mono.just(model.tokens().nl());
  }

  @Override
  public Mono<List<Integer>> tokenize(String text, boolean addBos, boolean enableControlCharacters) {
    return Mono.just(model.tokens().tokenize(text, addBos, enableControlCharacters));
  }

  @Override
  public Mono<String> detokenize(List<Integer> tokens) {
    return Mono.just(model.tokens().detokenize(tokens));
  }

}
