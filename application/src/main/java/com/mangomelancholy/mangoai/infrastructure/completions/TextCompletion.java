package com.mangomelancholy.mangoai.infrastructure.completions;

import java.util.List;

public record TextCompletion(String id, String object, Long created, String model, List<CompletionChoice> choices, OpenAIUsage usage) {

}
