package net.jllama.examples.chat.infrastructure.llama2.chat;

import net.jllama.api.Context;

public interface ContextManager {
  public boolean isCached(String id);
  public Context checkoutContext(String id);
  public void releaseContext(String id);
}
