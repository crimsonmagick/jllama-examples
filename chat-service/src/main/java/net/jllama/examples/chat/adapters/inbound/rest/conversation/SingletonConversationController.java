package net.jllama.examples.chat.adapters.inbound.rest.conversation;

import java.util.List;
import java.util.stream.Collectors;
import net.jllama.examples.chat.application.conversation.ConversationSingletonServiceImpl;
import net.jllama.examples.chat.application.conversation.ExpressionValue;
import net.jllama.examples.chat.application.conversation.ports.primary.ConversationSingletonService;
import net.jllama.examples.chat.application.conversation.ports.primary.ConversationSummary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/chat")
public class SingletonConversationController {

  private static final Logger log = LogManager.getLogger(SingletonConversationController.class);
  private final ConversationSingletonService conversationSingletonService;

  public SingletonConversationController(
      final ConversationSingletonServiceImpl conversationSingletonService) {
    this.conversationSingletonService = conversationSingletonService;
  }

  @GetMapping("/singleton/conversations/ids")
  public Mono<List<String>> getConversationIds() {
    return conversationSingletonService.getConversationIds();
  }

  @GetMapping("/singleton/conversations/summaries")
  public Mono<List<ConversationSummary>> getConversations() {
    return conversationSingletonService.getSummaries();
  }

  @GetMapping("/singleton/conversations/{id}/expressions")
  public Mono<List<ExpressionJson>> getExpressions(@PathVariable String id) {
    return conversationSingletonService.getExpressions(id).map(values -> values.stream()
        .map(value -> new ExpressionJson(null, value.content(), value.actor().toString(), null))
        .collect(Collectors.toList())
    );
  }

  @PostMapping("/singleton/conversations/{id}/expressions")
  public Mono<ExpressionJson> sendExpression(@PathVariable String id,
      @RequestBody ExpressionJson expressionJson) {
    return conversationSingletonService.sendExpression(id, expressionJson.content())
        .map(expressionValue -> new ExpressionJson(id, expressionValue.content(),
            expressionValue.actor().toString(), null))
        .doOnError(SingletonConversationController::error);
  }

  @PostMapping("/singleton/conversations")
  public Mono<ExpressionJson> startConversation(@RequestBody ExpressionJson expressionJson) {
    return conversationSingletonService.startConversation(expressionJson.content())
        .map(conversation -> {
          final ExpressionValue lastExpression = conversation.getLastExpression();
          return new ExpressionJson(conversation.getConversationId(), lastExpression.content(),
              lastExpression.actor().toString(), conversation.getSummary());
        })
        .doOnError(SingletonConversationController::error);
  }

  private static void error(final Throwable throwable) {
    log.error("Error processing request.", throwable);
  }
}
