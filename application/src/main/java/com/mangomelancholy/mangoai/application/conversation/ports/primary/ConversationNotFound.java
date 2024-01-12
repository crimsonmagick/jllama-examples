package com.mangomelancholy.mangoai.application.conversation.ports.primary;

public class ConversationNotFound extends RuntimeException {

  public ConversationNotFound(final String conversationId) {
    super();
    this.conversationId = conversationId;
  }

  private final String conversationId;

  public String getConversationId() {
    return conversationId;
  }
}
