package com.mangomelancholy.mangoai.infrastructure.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;

@Builder
public record ChatResponse(String id, String object, long created, String model, Usage usage, List<Choice> choices) {

  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record ChatMessage(String role, String content, String name) {

    public ChatMessage {
      if (role == null || content == null) {
        throw new IllegalArgumentException("Role and content cannot be null");
      }
    }
  }

  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record Choice(ChatMessage message, String finish_reason, int index, Delta delta) {

  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record Delta(String content) {

  }

  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record Usage(int prompt_tokens, int completion_tokens, int total_tokens) {

  }
}
