package net.jllama.examples.chat.application.llama;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.jllama.examples.chat.application.llama.ports.secondary.ModelService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class ModelSingletonServiceImpl implements ModelSingletonService {

  private final ModelService modelService;

  @Override
  public Mono<SpecialTokens> getSpecialTokens() {
    return Mono.zip(modelService.getBos(), modelService.getEos(), modelService.getNl())
        .map(specialTokens -> new SpecialTokens(specialTokens.getT1(), specialTokens.getT2(),
            specialTokens.getT3()));
  }

  @Override
  public Mono<List<Integer>> tokenize(String text, boolean addBos, boolean enableControlCharacters) {
    return modelService.tokenize(text, addBos, enableControlCharacters);
  }

  @Override
  public Mono<String> detokenize(List<Integer> tokens) {
    return modelService.detokenize(tokens);
  }
}
