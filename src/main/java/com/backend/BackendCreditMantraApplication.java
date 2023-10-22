package com.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class BackendCreditMantraApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendCreditMantraApplication.class, args);
		
		System.out.println("Credit-Mantra-Application Is Running-Fine");

	}

}
