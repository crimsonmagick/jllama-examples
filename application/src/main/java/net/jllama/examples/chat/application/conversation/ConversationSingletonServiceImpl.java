package net.jllama.examples.chat.application.conversation;

import net.jllama.examples.chat.application.AiServiceResolver;
import net.jllama.examples.chat.application.conversation.ExpressionValue.ActorType;
import net.jllama.examples.chat.application.conversation.ports.primary.ConversationNotFound;
import net.jllama.examples.chat.application.conversation.ports.primary.ConversationSingletonService;
import net.jllama.examples.chat.application.conversation.ports.primary.ConversationSummary;
import net.jllama.examples.chat.application.conversation.ports.secondary.AiSingletonService;
import net.jllama.examples.chat.application.conversation.ports.secondary.ConversationRepository;
import net.jllama.examples.chat.application.conversation.ports.secondary.MemoryService;
import net.jllama.examples.chat.infrastructure.ModelInfoService;
import net.jllama.examples.chat.infrastructure.ModelInfoService.ModelType;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class ConversationSingletonServiceImpl implements ConversationSingletonService {

  private final AiServiceResolver aiServiceResolver;
  private final ConversationRepository conversationRepository;
  private final MemoryService memoryService;
  private final ModelInfoService modelInfoService;

  @Override
  public Mono<List<ExpressionValue>> getExpressions(final String conversationId) {
    return conversationRepository.getExpressions(conversationId)
        .switchIfEmpty(Mono.error(new ConversationNotFound(conversationId)))
        .map(ExpressionValue::fromRecord)
        .collect(Collectors.toList());
  }

  @Override
  public Mono<List<String>> getConversationIds() {
    return conversationRepository.getConversationIds().collect(Collectors.toList());
  }

  @Override
  public Mono<List<ConversationSummary>> getSummaries() {
    return conversationRepository.getConversationSummaries()
        .map(record -> new ConversationSummary(record.conversationId(), record.summary()))
        .collectList();
  }

  @Override
  public Mono<ConversationEntity> startConversation(final String messageContent, final String model) {
    final ModelType modelType = ModelType.fromString(model);
    final AiSingletonService aiSingletonService = aiServiceResolver.resolveSingletonService(modelType);
    final ExpressionValue conversationSeed = new ExpressionValue(
        modelInfoService.getInitialPrompt(modelType), ActorType.INITIAL_PROMPT, null);
    final ExpressionValue userGreeting = new ExpressionValue(messageContent, ActorType.USER, null);
    final ConversationEntity startOfConversation = new ConversationEntity(conversationSeed,
        userGreeting);
    return conversationRepository.create(
            startOfConversation.toRecord())
        .flatMap(conversationRecord -> {
          final ConversationEntity savedConversation = ConversationEntity.fromRecord(
              conversationRecord);
          return aiSingletonService.exchange(savedConversation, modelType)
              .flatMap(expressionValue -> conversationRepository.addExpression(
                  expressionValue.toRecord()))
              .map(expressionRecord -> savedConversation.addExpression(
                  ExpressionValue.fromRecord(expressionRecord)));
        });
  }

  @Override
  public Mono<ExpressionValue> sendExpression(final String conversationId,
      final String messageContent, final String model) {
    final ModelType modelType = ModelType.fromString(model);
    final AiSingletonService aiSingletonService = aiServiceResolver.resolveSingletonService(modelType);
    return conversationRepository.getConversation(conversationId)
        .switchIfEmpty(Mono.error(new ConversationNotFound(conversationId)))
        .flatMap(conversationRecord -> {
          final ExpressionValue requestExpression = new ExpressionValue(messageContent, ActorType.USER, conversationId);
          final ConversationEntity fullConversation = ConversationEntity.fromRecord(conversationRecord)
              .addExpression(requestExpression);
          final ConversationEntity rememberedConversation = memoryService.rememberConversation(
              fullConversation, model);
          return aiSingletonService.exchange(rememberedConversation, modelType)
              .flatMap(responseExpression ->
                  conversationRepository.addExpression(requestExpression.toRecord())
                      .then(conversationRepository.addExpression(responseExpression.toRecord()))
              );
        })
        .map(ExpressionValue::fromRecord);
  }
}
