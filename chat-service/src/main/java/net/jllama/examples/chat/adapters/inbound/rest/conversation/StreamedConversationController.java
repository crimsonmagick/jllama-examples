package net.jllama.examples.chat.adapters.inbound.rest.conversation;

import net.jllama.examples.chat.application.conversation.ConversationStreamedServiceImpl;
import net.jllama.examples.chat.application.conversation.ExpressionFragment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
public class StreamedConversationController {

  private static final Logger log = LogManager.getLogger(StreamedConversationController.class);
  private final ConversationStreamedServiceImpl conversationStreamedService;

  public StreamedConversationController(final ConversationStreamedServiceImpl conversationStreamedService) {
    this.conversationStreamedService = conversationStreamedService;
  }

  @PostMapping(value = "/streamed/conversations/{id}/expressions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_NDJSON_VALUE)
  public Flux<ExpressionFragment> sendExpression(@PathVariable String id, @RequestBody ExpressionJson expressionJson) {
    return conversationStreamedService.sendExpression(id, expressionJson.content())
        .doOnError(throwable -> {
          log.error("Error processing request.", throwable);
        });
  }

  @PostMapping(value = "/streamed/conversations", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_NDJSON_VALUE)
  public Flux<ExpressionFragment> startStreamedConversation(final @RequestBody ExpressionJson expressionJson) {
    return conversationStreamedService.startConversation(expressionJson.content())
        .doOnError(throwable -> {
          log.error("Error processing request.", throwable);
        });
  }

}
