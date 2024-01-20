package net.jllama.examples.chat.infrastructure.llama2.chat;

import static net.jllama.api.Context.SequenceType.TOKEN;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import net.jllama.api.Context;
import net.jllama.api.Model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ContextManagerImpl implements ContextManager {

  private final ConcurrentHashMap<String, Context> cachedContexts;
  private final Queue<Context> initialPool;
  private final Set<String> contextInUse;

  public ContextManagerImpl(final Model model,
      @Value("${context.pool.size}") final int contextPoolSize) {
    cachedContexts = new ConcurrentHashMap<>(contextPoolSize);
    initialPool = new ConcurrentLinkedQueue<>();
    contextInUse = ConcurrentHashMap.newKeySet();
    IntStream.range(0, contextPoolSize)
        .parallel()
        .mapToObj(i -> buildContext(model))
        .forEach(initialPool::add);
  }

  private Context buildContext(final Model model) {
    final int threads = Runtime.getRuntime().availableProcessors() / 2 - 1;
    final int contextSize = 4096;
    final Context context = model.newContext()
        .withDefaults()
        .evaluationThreads(threads)
        .batchEvaluationThreads(threads)
        .maximumBatchSize(contextSize)
        .contextLength(contextSize)
        .seed(ThreadLocalRandom.current().nextInt())
        .create();
    context.batch().type(TOKEN)
        .configure()
        .batchSize(contextSize)
        .update();
    return context;
  }

  @Override
  public boolean isCached(final String id) {
    return cachedContexts.containsKey(id);
  }

  @Override
  public synchronized Context checkoutContext(final String id) {
    try {
      final Context context;
      if (cachedContexts.containsKey(id)) {
        context = cachedContexts.get(id);
        while (contextInUse.contains(id)) {
          wait();
        }
      } else {
        if (initialPool.isEmpty()) {
          while (contextInUse.size() == cachedContexts.size()) {
            wait();
          }
          final String cachedId = cachedContexts.keySet().stream()
              .filter(idToCheck -> !contextInUse.contains(idToCheck))
              .findAny()
              .orElseThrow(() -> new ContextManagerException(
                  "Unable to find a free cache - there is likely a bug"));
          context = cachedContexts.remove(cachedId);
          context.clearSequences();
        } else {
          context = initialPool.poll();
        }
        cachedContexts.put(id, context);
      }
      contextInUse.add(id);
      return context;
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ContextManagerException("Unable to get a free Llama Context", e);
    }
  }

  @Override
  public synchronized void releaseContext(final String id) {
    if (!cachedContexts.containsKey(id)) {
      throw new ContextManagerException(String.format("Context with id=%s not found", id));
    }
    contextInUse.remove(id);
    notifyAll();
  }
}
