package net.jllama.examples.chat.adapters.inbound.rest.llama;

import lombok.RequiredArgsConstructor;
import net.jllama.examples.chat.application.llama.DetokenizationRequest;
import net.jllama.examples.chat.application.llama.DetokenizationResponse;
import net.jllama.examples.chat.application.llama.ModelSingletonService;
import net.jllama.examples.chat.application.llama.SpecialTokens;
import net.jllama.examples.chat.application.llama.TokenizationRequest;
import net.jllama.examples.chat.application.llama.TokenizationResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/llama")
public class SingletonLlamaController {

  private static final Logger log = LogManager.getLogger(SingletonLlamaController.class);
  private final ModelSingletonService modelSingletonService;

  @GetMapping("/tokens/special")
  public Mono<SpecialTokens> getConversationIds() {
    return modelSingletonService.getSpecialTokens();
  }

  @PostMapping("/tokens/tokenization")
  public Mono<TokenizationResponse> tokenize(@RequestBody TokenizationRequest tokenizationRequest) {
    if (tokenizationRequest.text() == null || tokenizationRequest.text().isBlank()) {
      throw new IllegalArgumentException("No text provided for tokenization.");
    }
    final boolean addBos = tokenizationRequest.addBos() != null && tokenizationRequest.addBos();
    final boolean enableControlCharacters = tokenizationRequest.enableControlCharacters() != null
        && tokenizationRequest.enableControlCharacters();
    return modelSingletonService.tokenize(tokenizationRequest.text(), addBos,
            enableControlCharacters)
        .map(TokenizationResponse::new)
        .doOnError(SingletonLlamaController::error);
  }

  @PostMapping("/tokens/detokenization")
  public Mono<DetokenizationResponse> detokenize(
      @RequestBody DetokenizationRequest detokenizationRequest) {
    if (detokenizationRequest.tokens() == null || detokenizationRequest.tokens().isEmpty()) {
      throw new IllegalArgumentException("No tokens provided for detokenization.");
    }
    return modelSingletonService.detokenize(detokenizationRequest.tokens())
        .map(DetokenizationResponse::new)
        .doOnError(SingletonLlamaController::error);
  }

  private static void error(final Throwable throwable) {
    log.error("Error processing request.", throwable);
  }
}
