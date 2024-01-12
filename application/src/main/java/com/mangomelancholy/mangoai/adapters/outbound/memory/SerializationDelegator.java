package com.mangomelancholy.mangoai.adapters.outbound.memory;

import com.mangomelancholy.mangoai.adapters.outbound.chat.ChatExpressionMapper;
import com.mangomelancholy.mangoai.adapters.outbound.davinci.CompletionExpressionSerializer;
import com.mangomelancholy.mangoai.application.conversation.ExpressionValue;
import java.security.InvalidParameterException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SerializationDelegator {

  final ChatExpressionMapper chatExpressionMapper;
  final CompletionExpressionSerializer completionSerializer;

  public String serializeAsString(final ExpressionValue expressionValue, final String model) {
    if (model.toLowerCase().startsWith("gpt")) {
      return chatExpressionMapper.mapExpression(expressionValue).content(); // ??? should we just map expressionValue.content() directly ???
    } else if (model.toLowerCase().startsWith("davinci")) {
      return completionSerializer.serializeExpression(expressionValue);
    } else if (model.toLowerCase().startsWith("llama")) {
      return expressionValue.content(); // FIXME this is adding system symbols
    }
    throw new InvalidParameterException(String.format("Unsupported model=%s provided", model));
  }
}
