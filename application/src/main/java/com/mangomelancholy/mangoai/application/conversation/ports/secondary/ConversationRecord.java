package com.mangomelancholy.mangoai.application.conversation.ports.secondary;

import java.util.List;

public record ConversationRecord(String conversationId, List<ExpressionRecord> expressions, String summary)  {

}
