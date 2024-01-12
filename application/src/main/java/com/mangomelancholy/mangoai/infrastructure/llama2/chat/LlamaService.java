package com.mangomelancholy.mangoai.infrastructure.llama2.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.jllama.api.Batch;
import net.jllama.api.Context;
import net.jllama.api.Context.SequenceType;
import net.jllama.api.Model;
import net.jllama.api.Sequence;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

// FIXME NOT THREAD SAFE
@Service
public class LlamaService {

  private final Model model;
  private final Context context;
  private final Batch batch;

  public LlamaService(Model model, Context context) {
    this.model = model;
    this.context = context;
    this.batch = context.batch()
        .type(SequenceType.TOKEN)
        .configure()
        .batchSize(4096)
        .get();
  }

  public Mono<String> singletonCompletion(final String toComplete) {
    return Mono.fromCallable(() -> completeAll(toComplete))
        .subscribeOn(Schedulers.boundedElastic());
  }

  public Flux<String> streamCompletion(final String toComplete) {
    return Flux.create(sink -> {
      Schedulers.boundedElastic().schedule(() -> {
        try {
          final int eosToken = model.tokens().eos();
          final int contextSize = context.getContextSize();
          final Sequence<Integer> sequence = Sequence.tokenSequence(1);
          final List<Integer> inputTokens = model.tokens().tokenize(toComplete, false, true);
          batch.stage(sequence.piece(inputTokens));

          context.evaluate(batch);

          List<Integer> previousTokens = new ArrayList<>();
          int token = sample(context.getLogits(sequence), previousTokens);

          for (int i = inputTokens.size() + 1;
              token != eosToken && i < contextSize && !sink.isCancelled(); i++) {
            sink.next(model.tokens().detokenize(token));
            previousTokens.add(token);
            batch.stage(sequence.piece(Collections.singletonList(token)));
            context.evaluate(batch);
            token = sample(context.getLogits(sequence), previousTokens);
          }
          context.clearSequences();
          sink.complete();
        } catch (final Exception e) {
          context.clearSequences();
          batch.clear();
          sink.error(e);
        }
      });
    });
  }

  private String completeAll(final String toComplete) {
    final int eosToken = model.tokens().eos();
    final int contextSize = context.getContextSize();
    final Sequence<Integer> sequence = Sequence.tokenSequence(1);
    final List<Integer> inputTokens = model.tokens().tokenize(toComplete, false, true);
    batch.stage(sequence.piece(inputTokens));

    context.evaluate(batch);

    List<Integer> outputTokens = new ArrayList<>();
    int token = sample(context.getLogits(sequence), outputTokens);
    outputTokens.add(token);

    for (int i = inputTokens.size() + 1; token != eosToken && i < contextSize; i++) {
      batch.stage(sequence.piece(Collections.singletonList(token)));
      context.evaluate(batch);
      token = sample(context.getLogits(sequence), outputTokens);
      outputTokens.add(token);
    }
    context.clearSequences();
    return model.tokens().detokenize(outputTokens);
  }


  private int sample(final List<Float> logits, final List<Integer> previousTokens) {
    return context.sampler(logits)
        .keepTopK(50)
        .applyTemperature(1.1f)
//        .keepMinP(0.4f)
//        .keepTopP(0.9f)
//        .applySoftmax()
//        .applyLocallyTypical(0.1f)
//        .applyTailFree(0.1f)
//        .applyRepetitionPenalties(previousTokens, 1f, 1.1f, 1.5f)
        .sample();

  }

}
