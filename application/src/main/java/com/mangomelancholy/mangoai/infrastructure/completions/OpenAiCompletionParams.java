package com.mangomelancholy.mangoai.infrastructure.completions;

import lombok.Builder;

@Builder
public record OpenAiCompletionParams(String model, Boolean stream, Double temperature, Integer max_tokens, String prompt,
                                     Double top_p, Double frequency_penalty, Double presence_penalty) {

}