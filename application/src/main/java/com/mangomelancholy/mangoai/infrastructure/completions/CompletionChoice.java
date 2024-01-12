package com.mangomelancholy.mangoai.infrastructure.completions;

public record CompletionChoice(String text, Integer index, String logprobs, String finish_reason) {

}
