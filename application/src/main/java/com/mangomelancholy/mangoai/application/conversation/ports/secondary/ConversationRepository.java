package com.mangomelancholy.mangoai.application.conversation.ports.secondary;

import com.mangomelancholy.mangoai.application.conversation.ports.primary.ConversationSummary;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ConversationRepository {
  Mono<ConversationRecord> create(ConversationRecord newConversation);
  Mono<ConversationRecord> getConversation(String conversationId);

  Flux<String> getConversationIds();

  Flux<ConversationRecord> getConversationSummaries();

  Flux<ExpressionRecord> getExpressions(String conversationId);
  Mono<ExpressionRecord> addExpression(ExpressionRecord expressionRecord);

}

