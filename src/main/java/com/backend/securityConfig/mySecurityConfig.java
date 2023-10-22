package com.backend.securityConfig;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.backend.JWTConfig.JWTAuthenticationFilter;
import com.backend.JWTConfig.JwtAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class mySecurityConfig {

	private static final Logger logger = LoggerFactory.getLogger(mySecurityConfig.class);

	@Autowired
	private JWTAuthenticationFilter jwtAuthenticationFilter;

	@Autowired
	private JwtAuthenticationEntryPoint entryPoint;

	private final LogoutHandler logoutHandler;

	@Bean
	public UserDetailsService userDetailService() {
		logger.info("Initializing UserDetailsService bean");
		return new userDetailsServiceImpl();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		logger.info("Initializing PasswordEncoder bean");
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		logger.info("Configuring SecurityFilterChain");

		http.cors(cors -> cors.configurationSource(corsConfigurationSource())).csrf(csrf -> {
			csrf.disable();
			logger.info("CSRF protection is disabled");
		}).formLogin(formLogin -> formLogin.disable()).authorizeHttpRequests((auth) -> {
			auth.requestMatchers("/signup/**").permitAll().requestMatchers("/login/**").permitAll()
					.requestMatchers("/test").permitAll().requestMatchers("/forgot-password").permitAll()
					.requestMatchers("/forgot/verify-otp").permitAll().requestMatchers("/reset-password").permitAll()
					.anyRequest().authenticated();
			logger.info("All requests are permitted");
		}).logout((logout) -> {
			logout.logoutUrl("/logout").addLogoutHandler(logoutHandler)
					.logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
					.permitAll();
		}).exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint)).sessionManagement((sessionManagement) -> {
			sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
			logger.info("Session management is configured");
		}).authenticationProvider((authenticationProvider())).headers(headerCustomizer -> {
			headerCustomizer.xssProtection(xssp -> xssp.headerValue(HeaderValue.ENABLED_MODE_BLOCK))
					.httpStrictTransportSecurity(hsts -> hsts.disable())
					.referrerPolicy(rp -> rp.policy(ReferrerPolicy.NO_REFERRER_WHEN_DOWNGRADE));
			logger.info("Security headers are customized");
		});

		http.addFilterAt(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		logger.info("Initializing DaoAuthenticationProvider bean");

		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();

		authenticationProvider.setUserDetailsService(this.userDetailService());
		authenticationProvider.setPasswordEncoder(passwordEncoder());

		return authenticationProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		logger.info("Initializing CorsConfigurationSource bean");

		CorsConfiguration configuration = new CorsConfiguration();
		// Allow requests from specified origins
		configuration.setAllowedOrigins(Arrays.asList("https://www.creditmantra.co.in"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST"));
		configuration.setAllowCredentials(true);
		configuration.setAllowedHeaders(Arrays.asList("*"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
		logger.info("Initializing HttpSessionEventPublisher bean");
		return new HttpSessionEventPublisher();
	}

	@Bean
	public SessionRegistry sessionRegistry() {
		logger.info("Initializing SessionRegistry bean");
		return new SessionRegistryImpl();
	}

	@Bean
	public SessionAuthenticationStrategy concurrentSessionControlAuthenticationStrategy() {
		logger.info("Initializing SessionAuthenticationStrategy bean");
		return new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry());
	}
}
