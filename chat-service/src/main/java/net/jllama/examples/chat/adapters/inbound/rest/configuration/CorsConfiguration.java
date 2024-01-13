package net.jllama.examples.chat.adapters.inbound.rest.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.reactive.CorsWebFilter;

@Configuration
public class CorsConfiguration {

  @Bean
  public CorsWebFilter corsFilter() {
    return new CorsWebFilter(exchange -> new org.springframework.web.cors.CorsConfiguration().applyPermitDefaultValues());
  }
}
