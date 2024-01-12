package com.mangomelancholy.mangoai.infrastructure.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mangomelancholy.mangoai.infrastructure.chat.ChatResponse.ChatMessage;
import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiChatParams(String model, List<ChatMessage> messages, Boolean stream, Double temperature, Integer max_tokens, Double top_p, List<String> stop, Double frequency_penalty, Double presence_penalty, Map<String, String> logit_bias, String user) {

}