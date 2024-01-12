package com.mangomelancholy.mangoai.adapters.inbound.rest;

import com.mangomelancholy.mangoai.infrastructure.User;
import com.mangomelancholy.mangoai.infrastructure.UserDao;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class UserController {

  private static final Logger log = LogManager.getLogger(UserController.class);
  private final UserDao userDao;

  @PostMapping(value = "/users")
  public Mono<Long> sendExpression(@RequestBody final User user) {
    return userDao.save(user);
  }

  @GetMapping(value = "/users")
  public Flux<User> startStreamedConversation() {
    return userDao.findAll();
  }

}
