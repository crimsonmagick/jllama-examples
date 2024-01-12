package net.jllama.examples.chat.adapters.outbound.llama;

import net.jllama.examples.chat.application.conversation.ConversationEntity;
import net.jllama.examples.chat.application.conversation.ExpressionValue;
import net.jllama.examples.chat.application.conversation.ExpressionValue.ActorType;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LlamaConversationSerializer {

  private static final String B_INST = "<s>[INST]";
  private static final String E_INST = "[/INST]";
  private static final String B_SYS = "<<SYS>>\n";
  private static final String E_SYS = "\n<</SYS>>\n\n";
  private static final String EOS = "</s>";

  /**
   * Serializes a Conversation into a format that can be used with Llama 2 chat.
   *
   * @param conversation to parse
   * @return a parsed String
   */
  public String serialize(final ConversationEntity conversation) {
    final List<ExpressionValue> expressions = conversation.getExpressions();
    final StringBuilder serialized = new StringBuilder();
    int i = 0;
    while (i < expressions.size()) {
      StringBuilder systemPrompt = new StringBuilder();
      StringBuilder userPrompt = new StringBuilder();
      StringBuilder response = new StringBuilder();
      // aggregate all system and user prompts until a response is encountered
      while (i < expressions.size()) {
        final ExpressionValue current = expressions.get(i);
        if (current.actor() == ActorType.PAL) {
          response.append(current.content());
          break;
        } else if (current.actor() == ActorType.USER) {
          userPrompt.append(current.content());
        } else {
          systemPrompt.append(current.content());
        }
        i++;
      }

      serialized.append(B_INST);
      if (!systemPrompt.isEmpty()) {
        serialized.append(B_SYS).append(systemPrompt).append(E_SYS);
      }
      serialized.append(userPrompt).append(E_INST);
      if (!response.isEmpty()) {
        // exchange was finished, add response and EOS
        serialized.append(response).append(EOS);
      }

      i++;
    }
    return serialized.toString();
  }

}
