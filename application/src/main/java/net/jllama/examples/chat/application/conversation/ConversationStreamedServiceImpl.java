package net.jllama.examples.chat.application.conversation;

import net.jllama.examples.chat.application.AiServiceResolver;
import net.jllama.examples.chat.application.conversation.ExpressionValue.ActorType;
import net.jllama.examples.chat.application.conversation.ports.primary.ConversationNotFound;
import net.jllama.examples.chat.application.conversation.ports.primary.ConversationStreamedService;
import net.jllama.examples.chat.application.conversation.ports.secondary.AiStreamedService;
import net.jllama.examples.chat.application.conversation.ports.secondary.ConversationRepository;
import net.jllama.examples.chat.application.conversation.ports.secondary.MemoryService;
import net.jllama.examples.chat.infrastructure.ModelInfoService;
import net.jllama.examples.chat.infrastructure.ModelInfoService.ModelType;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class ConversationStreamedServiceImpl implements
    ConversationStreamedService<ExpressionFragment> {

  private static final Logger log = LogManager.getLogger(ConversationStreamedServiceImpl.class);

  private final AiServiceResolver aiServiceResolver;
  private final ConversationRepository conversationRepository;
  private final MemoryService memoryService;
  private final ModelInfoService modelInfoService;

  @Override
  public Flux<ExpressionFragment> startConversation(final String messageContent, final String model) {
    final ModelType modelType = ModelType.fromString(model);
    final AiStreamedService aiStreamedService = aiServiceResolver.resolveStreamedService(modelType);
    final ExpressionValue conversationSeed =
        new ExpressionValue(modelInfoService.getInitialPrompt(ModelType.fromString(model)), ActorType.INITIAL_PROMPT, null);
    final ExpressionValue userGreeting = new ExpressionValue(messageContent, ActorType.USER, null);
    final ConversationEntity startOfConversation = new ConversationEntity(conversationSeed,
        userGreeting);
    return conversationRepository.create(startOfConversation.toRecord())
        .flatMapMany(conversationRecord -> {
          final ConversationEntity conversation = ConversationEntity.fromRecord(conversationRecord);
          final Flux<ExpressionFragment> fragmentStream = aiStreamedService.exchange(conversation, modelType)
              .publish()
              .autoConnect(2);
          fragmentStream.map(ExpressionFragment::contentFragment)
              .collect(Collectors.joining())
              .map(content -> new ExpressionValue(content, ActorType.PAL, conversation.getConversationId()))
              .flatMap(expressionValue -> conversationRepository.addExpression(expressionValue.toRecord()))
              .doOnError(throwable -> log.info("Error updating conversation with PAL response.", throwable))
              .subscribe();
          return fragmentStream;
        });
  }

  @Override
  public Flux<ExpressionFragment> sendExpression(final String conversationId, final String messageContent, final String model) {
    final ModelType modelType = ModelType.fromString(model);
    final AiStreamedService aiStreamedService = aiServiceResolver.resolveStreamedService(modelType);
    return conversationRepository.getConversation(conversationId)
        .switchIfEmpty(Mono.error(new ConversationNotFound(conversationId)))
        .map(ConversationEntity::fromRecord)
        .flatMapMany(retrievedConversation -> {
          final ExpressionValue requestExpression = new ExpressionValue(messageContent, ActorType.USER, conversationId);
          final ConversationEntity fullConversation = retrievedConversation.addExpression(requestExpression);
          final ConversationEntity rememberedConversation = memoryService.rememberConversation(fullConversation, model);
          final Flux<ExpressionFragment> fragmentStream = conversationRepository.addExpression(requestExpression.toRecord())
              .thenMany(aiStreamedService.exchange(rememberedConversation, modelType))
              .publish()
              .autoConnect(2);
          fragmentStream.map(ExpressionFragment::contentFragment)
              .collect(Collectors.joining())
              .map(content -> new ExpressionValue(content, ActorType.PAL, conversationId))
              .flatMap(expressionValue -> conversationRepository.addExpression(expressionValue.toRecord()))
              .doOnError(throwable -> log.info("Error updating conversation with PAL response.", throwable))
              .doOnSuccess(expressionValue -> log.info("all done! expressionValue={}", expressionValue))
              .subscribe();
          return fragmentStream;
        });
  }

}
