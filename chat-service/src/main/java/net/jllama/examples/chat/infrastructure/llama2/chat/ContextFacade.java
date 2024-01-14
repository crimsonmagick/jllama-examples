package net.jllama.examples.chat.infrastructure.llama2.chat;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.jllama.api.Batch;
import net.jllama.api.Context;
import net.jllama.api.Sampler;
import net.jllama.api.Sequence;
import net.jllama.api.batch.BatchSpecifier;

@RequiredArgsConstructor
public class ContextFacade {

  private final Context context;
  @Getter
  @Setter
  private int contextLength;

  public BatchSpecifier batch() {
    return context.batch();
  }

  public void clearSequences() {
    context.clearSequences();
  }

  public void evaluate(final Batch batch) {
    context.evaluate(batch);
  }

  public int getContextSize() {
    return context.getContextSize();
  }

  public List<Float> getLogits(Sequence<Integer> sequence) {
    return context.getLogits(sequence);
  }

  public Sampler sampler(final List<Float> logits) {
    return context.sampler(logits);
  }
}
