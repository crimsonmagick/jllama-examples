package com.mangomelancholy.mangoai.adapters.outbound.chat;

import com.mangomelancholy.mangoai.application.conversation.ConversationEntity;
import com.mangomelancholy.mangoai.application.conversation.ExpressionFragment;
import com.mangomelancholy.mangoai.application.conversation.ExpressionValue;
import com.mangomelancholy.mangoai.application.conversation.ports.secondary.AiStreamedService;
import com.mangomelancholy.mangoai.infrastructure.ModelInfoService.ModelType;
import com.mangomelancholy.mangoai.infrastructure.chat.OpenAIChatClient;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class GptChatStreamedService implements AiStreamedService {

  private static final Logger log = LogManager.getLogger(GptChatStreamedService.class);
  private final ChatExpressionMapper chatExpressionMapper;
  private final ChatUtility chatUtility;
  private final OpenAIChatClient openAIChatClient;

  @Override
  public Flux<ExpressionFragment> exchange(final ConversationEntity conversationEntity, ModelType modelType) {
    return openAIChatClient.streamed()
        .complete(chatExpressionMapper.mapConversation(conversationEntity), modelType)
        .flatMap(response -> {
          final ExpressionValue content = chatUtility.mapResponse(response, conversationEntity.getConversationId());
          if (content == null) {
            return Mono.empty();
          } else {
            return Mono.just(content);
          }
        })
        .index()
        .map(tuple -> new ExpressionFragment(tuple.getT2().content(), conversationEntity.getConversationId(), tuple.getT1()));
  }
}
