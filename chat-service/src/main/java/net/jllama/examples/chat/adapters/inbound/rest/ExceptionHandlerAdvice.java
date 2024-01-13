package net.jllama.examples.chat.adapters.inbound.rest;


import net.jllama.examples.chat.adapters.inbound.exceptions.AiWebError;
import net.jllama.examples.chat.application.conversation.ports.primary.ConversationNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class ExceptionHandlerAdvice {

  @ExceptionHandler(ConversationNotFound.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Mono<ResponseEntity<?>> handleYourSpecificException(ConversationNotFound ex) {
    final HttpStatusCode statusCode = HttpStatusCode.valueOf(404);
    final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(statusCode,
        String.format("Conversation with conversationId=%s could not be found.", ex.getConversationId()));
    final ResponseEntity<AiWebError> errorResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).body(new AiWebError(statusCode, problemDetail));
    return Mono.just(errorResponse);
  }
}
