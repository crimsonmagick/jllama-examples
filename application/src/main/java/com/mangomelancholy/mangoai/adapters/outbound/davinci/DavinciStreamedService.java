package com.mangomelancholy.mangoai.adapters.outbound.davinci;

import com.mangomelancholy.mangoai.application.conversation.ConversationEntity;
import com.mangomelancholy.mangoai.application.conversation.ExpressionFragment;
import com.mangomelancholy.mangoai.application.conversation.ports.secondary.AiStreamedService;
import com.mangomelancholy.mangoai.infrastructure.ModelInfoService.ModelType;
import com.mangomelancholy.mangoai.infrastructure.completions.OpenAICompletionsClient;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class DavinciStreamedService implements AiStreamedService {

  private static final Logger log = LogManager.getLogger(DavinciStreamedService.class);
  private final CompletionUtility completionUtility;
  private final OpenAICompletionsClient completionsClient;
  private final CompletionConversationSerializer completionConversationSerializer;

  @Override
  public Flux<ExpressionFragment> exchange(final ConversationEntity conversationEntity, final ModelType modelType) {
    final StringBuilder sb = new StringBuilder();
    final AtomicBoolean attributionRemoved = new AtomicBoolean(false);
    return completionsClient.streamed()
        .complete(completionConversationSerializer.serializeConversation(conversationEntity))
        .handle((textCompletion, sink) -> {
          final String contentFragment = completionUtility.extractChoiceText(textCompletion);
          if (attributionRemoved.get()) {
            sink.next(contentFragment);
          } else {
            sb.append(contentFragment);
            final String bufferedString = sb.toString();
            final int index = completionUtility.lastIndexOfAttribution(bufferedString);
            if (index != -1) {
              attributionRemoved.set(true);
              sink.next(completionUtility.normalizeChoice(bufferedString));
            }
          }
        })
        // in case Davinci gets confused with the prefix.
        .concatWith(Mono.defer(() -> {
          if (!attributionRemoved.get()) {
            return Mono.just(sb.toString());
          } else {
            return Mono.empty();
          }
        }))
        .index()
        .map(tuple -> new ExpressionFragment((String) tuple.getT2(), conversationEntity.getConversationId(), tuple.getT1()));
  }

}
