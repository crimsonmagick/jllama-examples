package com.mangomelancholy.mangoai.adapters.outbound.chat;

import com.mangomelancholy.mangoai.application.conversation.ConversationEntity;
import com.mangomelancholy.mangoai.application.conversation.ExpressionValue;
import com.mangomelancholy.mangoai.application.conversation.ports.secondary.AiSingletonService;
import com.mangomelancholy.mangoai.infrastructure.ModelInfoService.ModelType;
import com.mangomelancholy.mangoai.infrastructure.chat.ChatResponse.ChatMessage;
import com.mangomelancholy.mangoai.infrastructure.chat.OpenAIChatClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class GptChatSingletonService implements AiSingletonService {

  private static final Logger log = LogManager.getLogger(GptChatSingletonService.class);
  private final ChatExpressionMapper chatExpressionMapper;
  private final ChatUtility chatUtility;
  private final OpenAIChatClient openAIChatClient;

  @Override
  public Mono<ExpressionValue> exchange(final ConversationEntity conversationEntity, final ModelType modelType) {
    final List<ChatMessage> chatMessages = chatExpressionMapper.mapConversation(conversationEntity);
    return openAIChatClient.singleton().complete(chatMessages, modelType)
        .doOnError(throwable -> onError(throwable, chatMessages))
        .map(response -> chatUtility.mapResponse(response, conversationEntity.getConversationId()));
  }

  private void onError(final Throwable throwable, final List<ChatMessage> content) {
    final String responseBody;
    if (throwable instanceof WebClientResponseException) {
      responseBody = ((WebClientResponseException) throwable).getResponseBodyAsString();
    } else {
      responseBody = "NONE";
    }
    log.error("Error while sending messages, messages={}, responseBody={}", content,
        responseBody, throwable);
  }

}
