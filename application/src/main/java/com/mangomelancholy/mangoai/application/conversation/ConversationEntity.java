package com.mangomelancholy.mangoai.application.conversation;

import com.mangomelancholy.mangoai.application.conversation.ports.secondary.ConversationRecord;
import com.mangomelancholy.mangoai.application.conversation.ports.secondary.ExpressionRecord;
import java.util.ArrayList;
import java.util.List;

public class ConversationEntity {

  private final String conversationId;
  private final List<ExpressionValue> expressionValues;
  private final String summary;
  public ConversationEntity(final ExpressionValue systemPrompt, final ExpressionValue userGreeting) {
    assert systemPrompt != null;
    assert userGreeting != null && userGreeting.content() != null && !"".equals(userGreeting.content().trim());

    expressionValues = List.of(systemPrompt, userGreeting);
    conversationId = null;
    final int length = userGreeting.content().length();
    final int lastIndex = length < 128 ? length : 127;
    summary = userGreeting.content().substring(0, lastIndex);
  }
  public ConversationEntity(final String conversationId, final List<ExpressionValue> expressionValues, final String summary) {
    assert conversationId != null;
    assert expressionValues != null;

    this.conversationId = conversationId;
    this.expressionValues = expressionValues;
    this.summary = summary;
  }

  public ConversationEntity addExpression(final ExpressionValue newExpression) {
    final List<ExpressionValue> updatedExpressions = new ArrayList<>(expressionValues);
    updatedExpressions.add(newExpression);
    return new ConversationEntity(conversationId, updatedExpressions, summary);
  }

  public static ConversationEntity fromRecord(final ConversationRecord conversationRecord) {
    final List<ExpressionValue> expressionValues = conversationRecord.expressions().stream().map(ExpressionValue::fromRecord).toList();
    return new ConversationEntity(conversationRecord.conversationId(), expressionValues, conversationRecord.summary());
  }

  public String getConversationId() {
    return conversationId;
  }

  public List<ExpressionValue> getExpressions() {
    return expressionValues;
  }

  public ExpressionValue getLastExpression() {
    return expressionValues.get(expressionValues.size() - 1);
  }

  public String getSummary() {
    return summary;
  }

  public ConversationRecord toRecord() {
    final List<ExpressionRecord> expressionRecords = expressionValues.stream().map(ExpressionValue::toRecord).toList();
    return new ConversationRecord(conversationId, expressionRecords, summary);
  }

}
