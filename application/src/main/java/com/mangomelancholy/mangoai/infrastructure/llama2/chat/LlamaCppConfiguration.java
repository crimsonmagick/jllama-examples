package com.mangomelancholy.mangoai.infrastructure.llama2.chat;

import static net.jllama.api.Context.SequenceType.TOKEN;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ThreadLocalRandom;
import net.jllama.api.Context;
import net.jllama.api.Llama;
import net.jllama.api.Model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlamaCppConfiguration {

  @Bean
  Model llamaApiModel(@Value("#{systemEnvironment['LLAMA_MODEL_PATH']}") final String modelPath) {
    return Llama.library()
        .newModel()
        .withDefaults()
        .path(modelPath)
        .load();
  }

  @Bean
  Context llamaApiContext(final Model model) {
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
        .batchSize(contextSize);
    return context;
  }

  @PreDestroy
  void llamaFree() {
    Llama.library().close();
  }

}
