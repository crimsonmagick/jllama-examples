package net.jllama.examples.chat.application.conversation.ports.secondary;

import net.jllama.examples.chat.application.conversation.ConversationEntity;

public interface MemoryService {

  ConversationEntity rememberConversation(ConversationEntity conversation, String model);

}
