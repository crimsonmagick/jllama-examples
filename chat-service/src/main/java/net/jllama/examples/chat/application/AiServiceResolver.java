package net.jllama.examples.chat.application;

import static net.jllama.examples.chat.application.conversation.ConversationFormat.CHAT;

import net.jllama.examples.chat.adapters.outbound.llama.ChatSingletonService;
import net.jllama.examples.chat.adapters.outbound.llama.ChatStreamedService;
import net.jllama.examples.chat.application.conversation.ConversationFormat;
import net.jllama.examples.chat.application.conversation.ports.secondary.AiSingletonService;
import net.jllama.examples.chat.application.conversation.ports.secondary.AiStreamedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AiServiceResolver {

  private final ChatSingletonService chatSingletonService;
  private final ChatStreamedService chatStreamedService;

  public AiSingletonService resolveSingletonService(final ConversationFormat conversationFormat) {
    if (conversationFormat == CHAT) {
      return chatSingletonService;
    }
    throw new IllegalArgumentException(
        "Unsupported conversationFormat=" + conversationFormat.getValue());
  }

  public AiStreamedService resolveStreamedService(final ConversationFormat conversationFormat) {
    if (conversationFormat == CHAT) {
      return chatStreamedService;
    }
    throw new IllegalArgumentException(
        "Unsupported conversationFormat=" + conversationFormat.getValue());  }

}
