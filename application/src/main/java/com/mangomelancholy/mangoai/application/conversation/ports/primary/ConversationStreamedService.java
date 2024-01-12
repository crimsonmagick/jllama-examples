package com.mangomelancholy.mangoai.application.conversation.ports.primary;

import com.mangomelancholy.mangoai.application.conversation.ExpressionFragment;
import reactor.core.publisher.Flux;

public interface ConversationStreamedService<T extends ExpressionFragment> {

  Flux<T> startConversation(String messageContent, String model);

  Flux<T> sendExpression(String conversationId, String messageContent, String model);

}
