package com.mangomelancholy.mangoai.adapters.inbound.exceptions;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;

public class AiWebError implements ErrorResponse {

  private final HttpStatusCode  httpStatusCode;
  private final ProblemDetail problemDetail;

  public AiWebError(final HttpStatusCode httpStatusCode, final ProblemDetail problemDetail) {
    this.httpStatusCode = httpStatusCode;
    this.problemDetail = problemDetail;
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return httpStatusCode;
  }

  @Override
  public ProblemDetail getBody() {
    return problemDetail;
  }
}
