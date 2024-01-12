package com.mangomelancholy.mangoai.infrastructure.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangomelancholy.mangoai.infrastructure.ModelInfoService;
import com.mangomelancholy.mangoai.infrastructure.ModelInfoService.ModelType;
import com.mangomelancholy.mangoai.infrastructure.chat.ChatResponse.ChatMessage;
import java.util.List;
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
public class OpenAIChatClient {

  public interface Delegation<T extends Publisher<ChatResponse>> {

    T complete(List<ChatMessage> chatMessages, ModelType modelType);
  }

  private static final Logger log = LogManager.getLogger(OpenAIChatClient.class);
  private final String apiKey;
  private final ModelInfoService modelInfoService;
  private final ObjectMapper objectMapper;
  private final WebClient webClient;

  public OpenAIChatClient(@Value("#{systemEnvironment['PAL_SECRETS_AUTHKEY']}") final String apiKey,
      final ObjectMapper objectMapper, final ModelInfoService modelInfoService) {
    this.apiKey = apiKey;
    this.objectMapper = objectMapper;
    this.webClient = WebClient.create("https://api.openai.com/v1/");
    this.modelInfoService = modelInfoService;
  }


  public class SingletonDelegation implements Delegation<Mono<ChatResponse>> {

    public Mono<ChatResponse> complete(final List<ChatMessage> prompt, final ModelType modelType) {
      final OpenAiChatParams params = OpenAiChatParams.builder()
          .stream(false)
          .max_tokens(modelInfoService.getMaxResponseTokens(modelType))
          .model(modelType.modelString())
          .build();
      return OpenAIChatClient.this.complete(prompt, params)
          .bodyToMono(ChatResponse.class);
    }
  }

  public class StreamedDelegation implements Delegation<Flux<ChatResponse>> {

    public Flux<ChatResponse> complete(final List<ChatMessage> chatMessages, final ModelType modelType) {
      final OpenAiChatParams params = OpenAiChatParams.builder()
          .stream(true)
          .max_tokens(modelInfoService.getMaxResponseTokens(modelType))
          .model(modelType.modelString())
          .build();
      return OpenAIChatClient.this.complete(chatMessages, params)
          .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {
          })
          .filter(event -> !"[DONE]".equals(event.data()))
          .map(event -> {
            try {
              return objectMapper.readValue(event.data(), ChatResponse.class);
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

  private ResponseSpec complete(final List<ChatMessage> chatMessages, final OpenAiChatParams params) {
    final OpenAiChatParams request = OpenAiChatParams.builder()
        .model(params.model() == null ? "gpt-3" : params.model())
        .messages(chatMessages)
        .temperature(params.temperature() == null ? 1.0 : params.temperature())
        .max_tokens(params.max_tokens() == null ? 300 : params.max_tokens())
        .top_p(params.top_p() == null ? 1.0 : params.top_p())
        .frequency_penalty(params.frequency_penalty() == null ? 0.5 : params.frequency_penalty())
        .presence_penalty(0D)
        .stream(params.stream() != null && params.stream())
        .build();

    return webClient.post()
        .uri("chat/completions")
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer " + apiKey)
        .body(BodyInserters.fromValue(request))
        .retrieve();
  }

}
