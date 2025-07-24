package ru.yandex.practicum.eshop.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuth2Controller {

  @GetMapping("/auth")
  public ResponseEntity<Void> authenticated() {
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/secured")
  @Secured("view-profile")
  public ResponseEntity<Void> secured() {
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/deny")
  @Secured("role-that-does-not-exists")
  public ResponseEntity<Void> deny() {
    return ResponseEntity.noContent().build();
  }
}