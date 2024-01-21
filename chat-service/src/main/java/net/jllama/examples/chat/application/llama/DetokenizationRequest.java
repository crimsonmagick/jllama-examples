package net.jllama.examples.chat.application.llama;

import java.util.List;

public record DetokenizationRequest(List<Integer> tokens) {

}
