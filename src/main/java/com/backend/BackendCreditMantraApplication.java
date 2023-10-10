package com.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class BackendCreditMantraApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendCreditMantraApplication.class, args);
		
		System.out.println("Credit-Mantra-Application Is Running");
		
		
	}
	
	
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowedHeaders("*").allowedOriginPatterns("*").allowedMethods("GET", "POST").allowCredentials(true);
			}
		};
	}

}
