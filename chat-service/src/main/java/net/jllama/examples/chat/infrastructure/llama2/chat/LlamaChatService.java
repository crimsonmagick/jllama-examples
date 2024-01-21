package net.jllama.examples.chat.infrastructure.llama2.chat;

import static net.jllama.api.Context.SequenceType.TOKEN;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.jllama.api.Batch;
import net.jllama.api.Context;
import net.jllama.api.Model;
import net.jllama.api.Sequence;
import net.jllama.api.Sequence.SequenceId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * Responsible for generating text with llama, and providing metadata on the underlying cache
 * system
 */
@RequiredArgsConstructor
@Service
public class LlamaChatService {

  private static final int TOP_K = 50;
  private static final float TEMP = 1.1f;
  private static final int SEQUENCE_ID = 1;

  final ContextManager contextManager;
  final Model model;

  /**
   * "Completes" provided text through LlaMA engine evaluation and sampling until either the engine
   * returns EOS or the underlying context length has been reached.
   *
   * @param id         this is used to identify the associated context for caching purposes
   * @param toComplete text string to complete
   * @return
   */
  public Flux<String> complete(final String id, final String toComplete) {
    return Flux.create(sink ->
        Schedulers.boundedElastic().schedule(() -> {
          final Context context = contextManager.checkoutContext(id);
          final Batch batch = context.batch().type(TOKEN).get();
          try {
            final int eosToken = model.tokens().eos();
            final int contextSize = context.getContextSize();
            final SequenceId sequenceId = Sequence.sequenceId(SEQUENCE_ID);
            final Sequence<Integer> sequence;
            if (context.getSequences().containsKey(sequenceId)) {
              sequence = (Sequence<Integer>) context.getSequences().get(sequenceId);
            } else {
              sequence = Sequence.tokenSequence(SEQUENCE_ID);
            }
            final List<Integer> inputTokens = model.tokens().tokenize(toComplete, false, true);
            batch.stage(sequence.piece(inputTokens));

            context.evaluate(batch);

            int token = context.sampler(context.getLogits(sequence))
                .keepTopK(TOP_K)
                .applyTemperature(TEMP)
                .sample();

            for (int i = inputTokens.size() + 1;
                token != eosToken && i < contextSize && !sink.isCancelled(); i++) {
              sink.next(model.tokens().detokenize(token));
              batch.stage(sequence.piece(Collections.singletonList(token)));
              context.evaluate(batch);
              token = context.sampler(context.getLogits(sequence))
                  .keepTopK(TOP_K)
                  .applyTemperature(TEMP)
                  .sample();
            }
            sink.complete();
          } catch (final RuntimeException e) {
            batch.clear();
            sink.error(e);
          } finally {
            contextManager.releaseContext(id);
          }
        }));
  }

  /**
   * Indicates whether a context with the associated id is cached. This lets the caller know whether
   * if it can rely on a previous context for text completion.
   *
   * @param id associated with the context
   * @return true if a cachecd context is available, false otherwise
   */
  public boolean isCached(final String id) {
    return contextManager.isCached(id);
  }

}
