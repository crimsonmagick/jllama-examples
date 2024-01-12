package com.mangomelancholy.mangoai.application.conversation.ports.primary;

import com.mangomelancholy.mangoai.application.conversation.ConversationEntity;
import com.mangomelancholy.mangoai.application.conversation.ExpressionValue;
import java.util.List;
import reactor.core.publisher.Mono;

public interface ConversationSingletonService {

  Mono<List<ExpressionValue>> getExpressions(String conversationId);

  Mono<List<String>> getConversationIds();

  Mono<List<ConversationSummary>> getSummaries();

  Mono<ConversationEntity> startConversation(String messageContent, String model);

  Mono<ExpressionValue> sendExpression(String conversationId, String messageContent, String model);

}
