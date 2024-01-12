package net.jllama.examples.chat.application.conversation.ports.secondary;

import net.jllama.examples.chat.application.conversation.ConversationEntity;
import net.jllama.examples.chat.application.conversation.ExpressionValue;
import net.jllama.examples.chat.infrastructure.ModelInfoService.ModelType;
import reactor.core.publisher.Mono;

public interface AiSingletonService {

  Mono<ExpressionValue> exchange(final ConversationEntity conversationEntity, final ModelType modelType);

}
