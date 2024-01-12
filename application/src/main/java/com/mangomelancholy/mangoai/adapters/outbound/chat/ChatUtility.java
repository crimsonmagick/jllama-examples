package com.mangomelancholy.mangoai.adapters.outbound.chat;

import static com.mangomelancholy.mangoai.application.conversation.ExpressionValue.ActorType.PAL;

import com.mangomelancholy.mangoai.application.conversation.ExpressionValue;
import com.mangomelancholy.mangoai.infrastructure.chat.ChatResponse;
import com.mangomelancholy.mangoai.infrastructure.chat.ChatResponse.Choice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class ChatUtility {

  private static final Logger log = LogManager.getLogger(ChatUtility.class);

  public static class MissingMessage extends RuntimeException {

    public MissingMessage(final String message) {
      super(message);
    }
  }

  public ExpressionValue mapResponse(final ChatResponse response, final String conversationId) {
    // assumes there will only be one message in response
    final Choice choice = response.choices().stream()
        .findAny()
        .orElseThrow(() -> new RuntimeException("No choice provided by response."));
    final String content;
    if (choice.message() != null) {
      content = choice.message().content();
    } else if (choice.delta() != null) {
      content = choice.delta().content();
    } else {
      throw new MissingMessage("Neither \"message\" nor \"delta\" is present");
    }
    if (content == null) {
      return null;
    }
    return new ExpressionValue(content, PAL, conversationId);
  }
}

