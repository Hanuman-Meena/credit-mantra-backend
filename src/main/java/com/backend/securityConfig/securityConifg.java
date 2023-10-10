package com.backend.securityConfig;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.backend.dao.UserRepository;

@Configuration
@EnableWebSecurity
public class securityConifg {
	
	@Autowired
	private UserDetailsService getUserDetailsService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Bean
	public UserDetailsService getUserDetailService()
	{
		
		return new userDetailsServiceImpl();
	}
		
	
	@Bean
	public PasswordEncoder passwordEncoder()
	{
		
		return new BCryptPasswordEncoder();
	}
	

	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
		
		http
		        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(csrf -> csrf.disable())
				.sessionManagement((sessionManagement) -> sessionManagement
				    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
				    .sessionFixation().migrateSession()
				    .sessionAuthenticationStrategy(concurrentSessionControlAuthenticationStrategy())
				    .invalidSessionUrl("/login?expired=true"))
				.formLogin(formLogin -> formLogin.disable())
				.securityMatcher("/**")
		        .authorizeHttpRequests((auth) -> auth
		            .anyRequest().permitAll())
		        .headers(headerCustomizer -> headerCustomizer
		                .frameOptions(fo -> fo.deny())
		                .xssProtection(xssp -> xssp.headerValue(HeaderValue.ENABLED_MODE_BLOCK))
		                .httpStrictTransportSecurity(hsts -> hsts.disable())
		                .referrerPolicy(rp -> rp.policy(ReferrerPolicy.NO_REFERRER_WHEN_DOWNGRADE)));
				
		  	return http.build();
		
	}
	
	
	 @Bean     	 
	 public DaoAuthenticationProvider daoAuthenticationProvider()
	 {
		
		DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
		
		auth.setUserDetailsService(this.getUserDetailService());
		auth.setPasswordEncoder(passwordEncoder());
		
		
		return auth;	
	 }
	

 
	    @Bean
	    public CorsConfigurationSource corsConfigurationSource() {
	        CorsConfiguration configuration = new CorsConfiguration();

	        // Allow requests from all origins
	        configuration.setAllowedOrigins(Arrays.asList("*"));

	       
	        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "DELETE","OPTIONS"));
	        configuration.setAllowCredentials(true);
	        configuration.setAllowedHeaders(Arrays.asList("*"));
	  

	        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	        source.registerCorsConfiguration("/**", configuration);
	        return source;
	    }
	 
	 
	
	@Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public SessionAuthenticationStrategy concurrentSessionControlAuthenticationStrategy() {
        return new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry());
    }

}
