package net.jllama.examples.chat.infrastructure.encoding;

public class BufferFullException extends RuntimeException {

  public BufferFullException(String message) {
    super(message);
  }
}
