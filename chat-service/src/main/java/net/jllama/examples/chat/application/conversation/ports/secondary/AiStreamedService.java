package net.jllama.examples.chat.application.conversation.ports.secondary;

import net.jllama.examples.chat.application.conversation.ConversationEntity;
import net.jllama.examples.chat.application.conversation.ConversationFormat;
import net.jllama.examples.chat.application.conversation.ExpressionFragment;
import net.jllama.examples.chat.application.conversation.ExpressionValue;
import reactor.core.publisher.Flux;

public interface AiStreamedService {

  Flux<ExpressionFragment> exchange(final ConversationEntity conversationEntity);
  Flux<ExpressionFragment> exchange(final ExpressionValue expressionValue);

}
