package com.mangomelancholy.mangoai.application.conversation.ports.secondary;

import com.mangomelancholy.mangoai.application.conversation.ConversationEntity;
import com.mangomelancholy.mangoai.application.conversation.ExpressionValue;
import com.mangomelancholy.mangoai.infrastructure.ModelInfoService.ModelType;
import reactor.core.publisher.Mono;

public interface AiSingletonService {

  Mono<ExpressionValue> exchange(final ConversationEntity conversationEntity, final ModelType modelType);

}
