package net.jllama.examples.chat.adapters.outbound;

import static reactor.core.publisher.Flux.fromIterable;

import net.jllama.examples.chat.application.conversation.ports.secondary.ConversationRecord;
import net.jllama.examples.chat.application.conversation.ports.secondary.ConversationRepository;
import net.jllama.examples.chat.application.conversation.ports.secondary.ExpressionRecord;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Primary
@Repository
@RequiredArgsConstructor
public class ConversationRepositoryH2Impl implements ConversationRepository {

  private static final Logger log = LogManager.getLogger(ConversationRepositoryH2Impl.class);
  private final ConnectionFactory connectionFactory;

  @Override
  public Mono<ConversationRecord> create(final ConversationRecord newConversation) {
    final String conversationId = UUID.randomUUID().toString();
    final List<ExpressionRecord> updatedExpressions = newConversation.expressions().stream()
        .map(record -> new ExpressionRecord(record.content(), record.actor(), conversationId)).toList();
    return Mono.usingWhen(connectionFactory.create(),
        connection -> Mono.from(connection.beginTransaction())
            .then(
                Mono.from(connection.createStatement("INSERT INTO CONVERSATIONS (ID, SUMMARY) VALUES (?, ?)")
                    .bind(0, conversationId)
                    .bind(1, newConversation.summary())
                    .execute())
            )
            .thenMany(fromIterable(newConversation.expressions()))
            .index()
            .flatMap(expressionTuple ->
                connection.createStatement(
                        "INSERT INTO EXPRESSIONS (CONTENT, ACTOR_TYPE, CONVERSATION_ID, SEQUENCE_NUMBER) VALUES (?, ?, ?, ?)")
                    .bind(0, expressionTuple.getT2().content())
                    .bind(1, expressionTuple.getT2().actor())
                    .bind(2, conversationId)
                    .bind(3, expressionTuple.getT1())
                    .execute()
            )
            .then(Mono.from(connection.commitTransaction()))
            .thenReturn(new ConversationRecord(conversationId, updatedExpressions, newConversation.summary()))
            .doOnError(throwable -> log.error(
                "Failed to create conversation with conversationId={}", conversationId,
                throwable))
            .onErrorResume(throwable -> Mono.from(connection.rollbackTransaction())
                .then(Mono.error(throwable))),
        Connection::close);
  }

  @Override
  public Mono<ConversationRecord> getConversation(final String conversationId) {
    return Mono.usingWhen(connectionFactory.create(),
            connection ->
                Mono.from(connection.createStatement("SELECT SUMMARY FROM CONVERSATIONS WHERE ID = ?")
                    .bind(0, conversationId)
                    .execute()),
            Connection::close)
        .flatMap(result ->
            Mono.from(result.map((row, metadata) -> row.get(0, String.class))))
        .flatMap(summary -> getExpressions(conversationId)
            .collectList()
            .map(expressionRecords -> new ConversationRecord(conversationId, expressionRecords, summary)));
  }

  @Override
  public Flux<String> getConversationIds() {
    return Flux.usingWhen(connectionFactory.create(),
            connection -> connection
                .createStatement("SELECT ID FROM CONVERSATIONS")
                .execute(),
            Connection::close)
        .flatMap(result -> result.map((row, meta) -> row.get("ID", String.class)));
  }

  @Override
  public Flux<ConversationRecord> getConversationSummaries() {
    return Flux.usingWhen(connectionFactory.create(),
            connection -> connection
                .createStatement("SELECT ID, SUMMARY FROM CONVERSATIONS")
                .execute(),
            Connection::close)
        .flatMap(result -> result.map((row, meta) -> {
          final String id = row.get("ID", String.class);
          final String summary = row.get("SUMMARY", String.class);
          return new ConversationRecord(id, null, summary);
        }));
  }

  @Override
  public Flux<ExpressionRecord> getExpressions(final String conversationId) {
    return Flux.usingWhen(connectionFactory.create(),
            connection -> connection
                .createStatement(
                    "SELECT CONTENT, ACTOR_TYPE, CONVERSATION_ID, SEQUENCE_NUMBER FROM EXPRESSIONS WHERE CONVERSATION_ID = ? ORDER BY SEQUENCE_NUMBER")
                .bind(0, conversationId)
                .execute(),
            Connection::close)
        .flatMap(result -> result.map((row, meta) -> {
          final String content = row.get("CONTENT", String.class);
          final String actorType = row.get("ACTOR_TYPE", String.class);
          return new ExpressionRecord(content, actorType, conversationId);
        }));
  }


  @Override
  public Mono<ExpressionRecord> addExpression(final ExpressionRecord expressionRecord) {
    return Mono.usingWhen(connectionFactory.create(),
        connection ->
            Mono.from(connection.beginTransaction())
                .then(Mono.from(connection.createStatement("SELECT MAX(SEQUENCE_NUMBER) FROM EXPRESSIONS WHERE CONVERSATION_ID = ?")
                    .bind(0, expressionRecord.conversationId())
                    .execute()))
                .flatMap(result -> Mono.from(result.map(
                    (row, rowMetadata) -> row.get(0, Integer.class))
                ))
                .flatMap(sequenceNumber -> Mono.from(connection.createStatement(
                            "INSERT INTO EXPRESSIONS (CONTENT, ACTOR_TYPE, CONVERSATION_ID, SEQUENCE_NUMBER) VALUES (?, ?, ?, ?)")
                        .bind(0, expressionRecord.content())
                        .bind(1, expressionRecord.actor())
                        .bind(2, expressionRecord.conversationId())
                        .bind(3, sequenceNumber)
                        .execute())
                    .then(Mono.from(connection.commitTransaction()))
                    .doOnError(throwable -> log.error(
                        "Failed to create expression with conversationId={}, sequenceNumber={}",
                        expressionRecord.conversationId(), sequenceNumber, throwable))
                    .onErrorResume(throwable -> Mono.from(connection.rollbackTransaction())
                        .then(Mono.error(throwable)))
                )
                .thenReturn(new ExpressionRecord(expressionRecord.content(), expressionRecord.actor(), expressionRecord.conversationId())),
        Connection::close);
  }

}
