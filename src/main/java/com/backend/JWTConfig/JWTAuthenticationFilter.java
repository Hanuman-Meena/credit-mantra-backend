package com.backend.JWTConfig;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.backend.ExceptionHandler.CustomUserNameNotFoundException;
import com.backend.securityConfig.userDetailsServiceImpl;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtHelper jwtHelper;

	@Autowired
	private userDetailsServiceImpl userDetailsService;

	private static final Logger logger = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String token = null;
		String username = null;

		logger.info("token from JWTAuthFilter : " + token);

		try {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("token".equals(cookie.getName())) {
						token = cookie.getValue();
						break;
					}
				}
			}

			if (token != null) {

				System.out.println("Running fine till here!!");
				username = jwtHelper.getUserNameFromToken(token);

				System.out.println("username : " + username);

			} else {
				logger.info("Token not found!!");

				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

			}

		} catch (IllegalArgumentException e) {

			logger.error("An error occured : " + e.getMessage());

			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (MalformedJwtException e) {

			logger.error("An error occured : " + e.getMessage());

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} catch (ExpiredJwtException e) {

			logger.error("An error occured : " + e.getMessage());

			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		} catch (CustomUserNameNotFoundException e) {
			logger.error("user is not registered : " + e.getMessage());

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} catch (UsernameNotFoundException e) {
			logger.error("User is not registered : " + e.getMessage());

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} catch (Exception e) {

			logger.error("An error occured : " + e.getMessage());

			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

			System.out.println("About to get user details  with username(phoneNumber): " + username);

			UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

			System.out.println("UserDetails from loadUserByUserName : " + userDetails.getUsername());
			Boolean validateToken = this.jwtHelper.validateToken(token, userDetails);

			if (validateToken) {

				System.out.println("from if  block validateToken");
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} else {
				logger.info("Validation failed!!");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}

		filterChain.doFilter(request, response);

	}

}
