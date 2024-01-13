package net.jllama.examples.chat.adapters.outbound;

import net.jllama.examples.chat.application.conversation.ports.secondary.ConversationRecord;
import net.jllama.examples.chat.application.conversation.ports.secondary.ConversationRepository;
import net.jllama.examples.chat.application.conversation.ports.secondary.ExpressionRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class ConversationRepositoryHashmapImpl implements ConversationRepository {

  final Map<String, ConversationRecord> conversations;

  public ConversationRepositoryHashmapImpl() {
    conversations = new ConcurrentHashMap<>();
  }

  @Override
  public Mono<ConversationRecord> create(final ConversationRecord newConversation) {
    final String conversationId = UUID.randomUUID().toString();
    final ConversationRecord newRecord = new ConversationRecord(conversationId,
        newConversation.expressions(), newConversation.summary());
    conversations.put(conversationId, newRecord);
    return Mono.just(newRecord);
  }

  @Override
  public Mono<ConversationRecord> getConversation(final String conversationId) {
    final ConversationRecord conversationRecord = conversations.get(conversationId);
    if (conversationRecord == null) {
      return Mono.empty();
    }
    return Mono.just(conversationRecord);
  }

  @Override
  public Flux<String> getConversationIds() {
    return Flux.fromIterable(conversations.keySet());
  }

  @Override
  public Flux<ConversationRecord> getConversationSummaries() {
    return Flux.fromIterable(conversations.entrySet().stream()
        .map(conversationEntry ->
            new ConversationRecord(conversationEntry.getKey(), null,
                conversationEntry.getValue().summary()))
        .collect(Collectors.toList()));
  }

  @Override
  public Flux<ExpressionRecord> getExpressions(final String conversationId) {
    ConversationRecord conversationRecord = conversations.get(conversationId);
    if (conversationRecord == null) {
      return Flux.empty();
    } else {
      return Flux.fromIterable(conversationRecord.expressions());
    }
  }

  @Override
  public Mono<ExpressionRecord> addExpression(final ExpressionRecord expressionRecord) {
    final ConversationRecord conversation = conversations.get(expressionRecord.conversationId());
    if (conversation == null) {
      return Mono.empty();
    }
    final List<ExpressionRecord> expressions = new ArrayList<>(conversation.expressions());
    expressions.add(expressionRecord);
    conversations.put(expressionRecord.conversationId(),
        new ConversationRecord(conversation.conversationId(), expressions, conversation.summary()));
    return Mono.just(expressionRecord);
  }
}
