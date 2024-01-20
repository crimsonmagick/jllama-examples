package net.jllama.examples.chat.infrastructure.llama2.chat;

public class ContextManagerException extends RuntimeException {

  ContextManagerException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public ContextManagerException(String message) {

  }
}
