package net.jllama.examples.chat.application.conversation;

public enum ConversationFormat {
  CHAT("CHAT");

  private final String formatType;

  ConversationFormat(final String formatType) {
    this.formatType = formatType;
  }

  public String getValue() {
    return formatType;
  }

  public static ConversationFormat fromString(final String modelName) {
    if (modelName.startsWith("chat")) {
      return CHAT;
    }
    throw new IllegalArgumentException("Unrecognized model format.");
  }
}