package com.backend.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
  @GetMapping(value = "/")
  public String greet() {
    return "Credit_Mantra_Application_Running";
  }

}
