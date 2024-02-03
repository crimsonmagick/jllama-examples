package net.jllama.examples.chat.application.llama;

import java.util.List;

public record TokenizationRequest(String text, Boolean addBos, Boolean enableControlCharacters) {

}
