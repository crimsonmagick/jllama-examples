package com.mangomelancholy.mangoai.infrastructure.h2;

import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import reactor.core.publisher.Mono;

@Configuration
public class DatabaseConfiguration {

  @Bean
  public ConnectionFactory connectionFactory() {
    return new H2ConnectionFactory(
        H2ConnectionConfiguration.builder()
            .file("/pal/db/paldb")
            .username("sa")
            .password("")
            .build()
    );
  }


  @Bean
  public ApplicationListener<ContextRefreshedEvent> initializer(final ConnectionFactory connectionFactory) {
    return event -> {
      final String schema;
      try {
        schema = FileCopyUtils.copyToString(new InputStreamReader(
            new ClassPathResource("schema.sql").getInputStream(), StandardCharsets.UTF_8));
      } catch (IOException e) {
        throw new RuntimeException("Unable to read schema.sql", e);
      }
      Mono.from(connectionFactory.create())
          .flatMapMany(connection ->
              Mono.from(connection.beginTransaction()) // Start a transaction
                  .thenMany(Mono.from(connection
                          .createBatch()
                          .add(schema)
                          .execute())
                      .then(Mono.from(connection.commitTransaction())) // Commit the transaction
                      .onErrorResume(throwable -> Mono.from(connection.rollbackTransaction())) // Rollback the transaction if an error occurs
                  )
                  .then(Mono.from(connection.close())) // Close the connection
          )
          .then()
          .block();
    };
  }
}
