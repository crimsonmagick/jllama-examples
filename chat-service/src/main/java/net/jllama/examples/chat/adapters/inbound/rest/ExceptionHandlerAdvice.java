package net.jllama.examples.chat.adapters.inbound.rest;


import net.jllama.examples.chat.adapters.inbound.exceptions.AiWebError;
import net.jllama.examples.chat.application.conversation.ports.primary.ConversationNotFound;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class ExceptionHandlerAdvice {

  @ExceptionHandler(ConversationNotFound.class)
  public Mono<ResponseEntity<?>> handleMissingConversation(final ConversationNotFound ex) {
    final HttpStatusCode statusCode = HttpStatusCode.valueOf(404);
    final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(statusCode,
        String.format("Conversation with conversationId=%s could not be found.",
            ex.getConversationId()));
    final ResponseEntity<AiWebError> errorResponse = ResponseEntity.status(statusCode)
        .body(new AiWebError(statusCode, problemDetail));
    return Mono.just(errorResponse);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public Mono<ResponseEntity<?>> handleIllegalArguments(final IllegalArgumentException ex) {
    final HttpStatusCode statusCode = HttpStatusCode.valueOf(400);
    final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(statusCode,
        ex.getMessage());
    final ResponseEntity<AiWebError> errorResponse = ResponseEntity.status(statusCode)
        .body(new AiWebError(statusCode, problemDetail));
    return Mono.just(errorResponse);
  }
}
