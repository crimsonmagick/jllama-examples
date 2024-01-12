package com.mangomelancholy.mangoai.application.conversation;

import com.mangomelancholy.mangoai.application.conversation.ports.secondary.ExpressionRecord;

public record ExpressionValue(String content, ActorType actor, String conversationId) {

  public enum ActorType {
    INITIAL_PROMPT, USER, SYSTEM, PAL
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
