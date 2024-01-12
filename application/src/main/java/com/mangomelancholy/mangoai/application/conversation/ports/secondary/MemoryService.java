package com.mangomelancholy.mangoai.application.conversation.ports.secondary;

import com.mangomelancholy.mangoai.application.conversation.ConversationEntity;

public interface MemoryService {

  ConversationEntity rememberConversation(ConversationEntity conversation, String model);

}
