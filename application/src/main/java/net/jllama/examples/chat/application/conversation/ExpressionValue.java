package net.jllama.examples.chat.application.conversation;

import net.jllama.examples.chat.application.conversation.ports.secondary.ExpressionRecord;

public record ExpressionValue(String content, ActorType actor, String conversationId) {

  public enum ActorType {
    USER, SYSTEM, AGENT
  }

  public ExpressionValue {
    assert content != null;
    assert actor != null;
  }

  public static ExpressionValue fromRecord(final ExpressionRecord expressionRecord) {
    return new ExpressionValue(expressionRecord.content(), ActorType.valueOf(expressionRecord.actor()), expressionRecord.conversationId());
  }

  public ExpressionRecord toRecord() {
    return new ExpressionRecord(content, actor.toString(), conversationId);
  }
}
