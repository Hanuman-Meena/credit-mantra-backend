package com.backend.Controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MainController {
	

    @GetMapping(value = "/test")
    public String greet() {
      
        return "Backend_CM_App_Running_Fine";
    }
    
}

