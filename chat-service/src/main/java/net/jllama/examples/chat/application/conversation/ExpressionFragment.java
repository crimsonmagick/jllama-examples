package net.jllama.examples.chat.application.conversation;

public record ExpressionFragment(String contentFragment, String conversationId, long sequenceNumber) {

  public ExpressionFragment {
    assert contentFragment != null;
    assert conversationId != null;
    assert sequenceNumber > 0;
  }
}
