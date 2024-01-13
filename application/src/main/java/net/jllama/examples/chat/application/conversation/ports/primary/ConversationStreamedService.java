package net.jllama.examples.chat.application.conversation.ports.primary;

import net.jllama.examples.chat.application.conversation.ExpressionFragment;
import reactor.core.publisher.Flux;

public interface ConversationStreamedService<T extends ExpressionFragment> {

  Flux<T> startConversation(String messageContent);

  Flux<T> sendExpression(String conversationId, String messageContent);

}
