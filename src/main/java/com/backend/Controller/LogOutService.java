package com.backend.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogOutService implements LogoutHandler {
	
	
	private final Logger logger = LoggerFactory.getLogger(LogOutService.class);
	

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		
		
		logger.info("Logging out from the application!!");
		
		String cookieValue = "token=" + null + "; Secure; SameSite=None; Max-Age=" + 1 + "; Path=/; Domain=backend.creditmantra.co.in; HttpOnly";
		response.setHeader("Set-Cookie", cookieValue);


	
	}

}
