package com.mangomelancholy.mangoai.infrastructure.completions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangomelancholy.mangoai.infrastructure.ModelInfoService;
import com.mangomelancholy.mangoai.infrastructure.ModelInfoService.ModelType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class OpenAICompletionsClient {

  public interface Delegation<T extends Publisher<TextCompletion>> {

    T complete(String prompt);
  }

  private static final Logger log = LogManager.getLogger(OpenAICompletionsClient.class);
  private final String apiKey;
  private final ModelInfoService modelInfoService;
  private final ObjectMapper objectMapper;
  private final WebClient webClient;

  public OpenAICompletionsClient(@Value("${pal.secrets.authkey}") final String apiKey,
      final ObjectMapper objectMapper, final ModelInfoService modelInfoService) {
    this.apiKey = apiKey;
    this.objectMapper = objectMapper;
    this.webClient = WebClient.create("https://api.openai.com/v1/");
    this.modelInfoService = modelInfoService;
  }


  public class SingletonDelegation implements Delegation<Mono<TextCompletion>> {

    public Mono<TextCompletion> complete(final String prompt) {
      final OpenAiCompletionParams params = OpenAiCompletionParams.builder()
          .stream(false)
          .max_tokens(modelInfoService.getMaxResponseTokens(ModelType.DAVINCI))
          .build();
      return OpenAICompletionsClient.this.complete(prompt, params)
          .bodyToMono(TextCompletion.class);
    }
  }

  public class StreamedDelegation implements Delegation<Flux<TextCompletion>> {

    public Flux<TextCompletion> complete(final String prompt) {
      final OpenAiCompletionParams params = OpenAiCompletionParams.builder()
          .stream(true)
          .max_tokens(modelInfoService.getMaxResponseTokens(ModelType.DAVINCI))
          .build();
      return OpenAICompletionsClient.this.complete(prompt, params)
          .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {
          })
          .filter(event -> !"[DONE]".equals(event.data()))
          .map(event -> {
            try {
              return objectMapper.readValue(event.data(), TextCompletion.class);
            } catch (final JsonProcessingException e) {
              throw new RuntimeException(
                  String.format("Unable to parse data value. data=%s", event.data()), e);
            }
          })
          .onErrorContinue(
              (throwable, event) -> log.warn("Error processing event. event={}", event, throwable));
    }
  }

  public SingletonDelegation singleton() {
    return new SingletonDelegation();
  }

  public StreamedDelegation streamed() {
    return new StreamedDelegation();
  }

  private ResponseSpec complete(final String prompt, OpenAiCompletionParams params) {
    final OpenAiCompletionParams request = OpenAiCompletionParams.builder()
        .model(params.model() == null ? "text-davinci-003" : params.model())
        .prompt(prompt)
        .temperature(params.temperature() == null ? 0.9 : params.temperature())
        .max_tokens(params.max_tokens() == null ? 300 : params.max_tokens())
        .top_p(params.top_p() == null ? 0.3 : params.top_p())
        .frequency_penalty(params.frequency_penalty() == null ? 0.5 : params.frequency_penalty())
        .presence_penalty(0D)
        .stream(params.stream() != null && params.stream())
        .build();

    return webClient.post()
        .uri("completions")
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer " + apiKey)
        .body(BodyInserters.fromValue(request))
        .retrieve();
  }

}
