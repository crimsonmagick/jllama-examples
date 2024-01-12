package net.jllama.examples.chat.infrastructure;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class UserDao {

  private final ConnectionFactory connectionFactory;

  public UserDao(ConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  public Flux<User> findAll() {
    String query = "SELECT * FROM users";
    return Mono.from(connectionFactory.create())
        .flatMapMany(connection ->
            Flux.from(connection.createStatement(query)
                    .execute())
                .flatMap(result -> result
                    .map((row, rowMetadata) -> new User(
                        row.get("id", Long.class),
                        row.get("name", String.class),
                        row.get("email", String.class))))
                .doFinally(signalType -> connection.close())
        );
  }

  public Mono<Long> save(final User user) {
    final String query = "INSERT INTO users(name, email) VALUES($1, $2)";
    final Mono<Long> longMono = Mono.from(connectionFactory.create())
        .flatMap(connection ->
            Mono.from(connection.createStatement(query)
                    .bind("$1", user.name())
                    .bind("$2", user.email())
                    .execute())
                .flatMap(result -> Mono.from(result.getRowsUpdated()))
                .doFinally(signalType -> connection.close())
        );
    return longMono;
  }
}
