package com.mangomelancholy.mangoai.adapters.outbound.chat;

import com.mangomelancholy.mangoai.application.conversation.ConversationEntity;
import com.mangomelancholy.mangoai.application.conversation.ExpressionValue;
import com.mangomelancholy.mangoai.application.conversation.ExpressionValue.ActorType;
import com.mangomelancholy.mangoai.infrastructure.chat.ChatResponse.ChatMessage;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ChatExpressionMapper {

  /**
   * Maps an Expression into a structured object that can be used with the chat OpenAI client.
   *
   * @param expression to map
   * @return a ChatMessage object
   */
  public ChatMessage mapExpression(final ExpressionValue expression) {
    return ChatMessage.builder()
        .content(expression.content())
        .role(getRole(expression.actor()))
        .build();
  }

  public List<ChatMessage> mapConversation(final ConversationEntity conversation) {
    return conversation.getExpressions()
        .stream()
        .map(this::mapExpression)
        .collect(Collectors.toList());
  }

  private String getRole(final ActorType actorType) {
    if (actorType == ActorType.INITIAL_PROMPT) {
      return "system";
    } else if (actorType == ActorType.USER) {
      return "user";
    } else if (actorType == ActorType.SYSTEM) {
      return "system";
    } else {
      return "assistant";
    }
  }

}
