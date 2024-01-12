package com.mangomelancholy.mangoai.adapters.outbound.davinci;

import com.mangomelancholy.mangoai.application.conversation.ExpressionValue;
import com.mangomelancholy.mangoai.application.conversation.ExpressionValue.ActorType;
import com.mangomelancholy.mangoai.infrastructure.completions.TextCompletion;
import org.springframework.stereotype.Component;

@Component
public class CompletionUtility {

  protected static final String ATTRIBUTION = ActorType.PAL + ":";

  public String extractChoiceText(final TextCompletion textCompletion) {
    return textCompletion.choices().stream()
        .findFirst()
        .orElseThrow(() -> new RuntimeException("No choice provided by completion."))
        .text();
  }

  public int lastIndexOfAttribution(final String content) {
    return content.lastIndexOf(ATTRIBUTION);
  }

  public ExpressionValue mapExpressionValue(final TextCompletion response, final String conversationId) {
    final String choiceText = extractChoiceText(response);
    final String normalizedChoiceText = normalizeChoice(choiceText);
    return new ExpressionValue(normalizedChoiceText, ActorType.PAL, conversationId);
  }

  public String normalizeChoice(final String choiceText) {
    final int attributionIndex = lastIndexOfAttribution(choiceText);
    final String palResponse;
    if (attributionIndex > -1) {
      palResponse = choiceText.substring(attributionIndex + ATTRIBUTION.length());
    } else {
      palResponse = choiceText;
    }
    return palResponse;
  }
}

