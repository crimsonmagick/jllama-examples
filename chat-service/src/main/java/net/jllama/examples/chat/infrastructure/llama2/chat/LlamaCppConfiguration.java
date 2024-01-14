package net.jllama.examples.chat.infrastructure.llama2.chat;

import jakarta.annotation.PreDestroy;
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
  @PreDestroy
  void llamaFree() {
    Llama.library().close();
  }

}
