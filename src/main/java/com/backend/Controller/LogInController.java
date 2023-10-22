package com.backend.Controller;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.backend.DTO.UserDTO;
import com.backend.ExceptionHandler.CustomUserNameNotFoundException;
import com.backend.JWTConfig.JwtHelper;
import com.backend.LogInResponse.AuthResponse;
import com.backend.LogInResponse.LogInResponse;
import com.backend.LogInResponse.VerifyLogInOTPResponse;
import com.backend.Service.AccountActivationService;
import com.backend.Service.TwilioService;
import com.backend.dao.UserRepository;
import com.backend.entity.User;
import com.backend.request.UserLogInRequest;
import com.backend.request.VerifyLogInOTPRequest;
import com.backend.securityConfig.userDetailsServiceImpl;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LogInController {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final TwilioService twilioService;
	
//	private final SnsService snsService;

	private final LogInResponse logInResponse;

	private final VerifyLogInOTPResponse verifyLogInOTPResponse;
	
	private final AuthResponse authResponse;

	private static final int MAX_WRONG_OTP_ATTEMPTS = 3;

	private static final int MAX_WRONG_LOGIN_ATTEMPTS = 3;
	
	Instant expirationDate = Instant.now().plus(Duration.ofDays(12));

	@Autowired
	private JwtHelper jwtHelper;
	
	@Autowired
	private userDetailsServiceImpl userDetailsServiceImpl;


	private final ConcurrentHashMap<String, Integer> otpStore = new ConcurrentHashMap<>();
	private Map<String, Integer> wrongOtpAttempts = new ConcurrentHashMap<>();

	private static final Logger logger = LoggerFactory.getLogger(LogInController.class);
	
	@Autowired
	private AccountActivationService accountActivationService;
	

	

	@GetMapping("/auth")
	public ResponseEntity<AuthResponse> checkAuth(HttpServletRequest request) {
	    String token = null;
	    
	    logger.info("Inside auth endpoint");
	    logger.info(token);
	    
	   
			
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
			    
			    logger.info("Token after trying to get from cookies : "+token);
			    
			    if (token == null) {
			    	logger.info("Token not found  in the cookie");
			        authResponse.setMessage("Token not found in the cookie.");
			        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResponse);
			    }
				UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(jwtHelper.getUserNameFromToken(token));
				
				System.out.println("Token from auth endpoint is : "+token);
				System.out.println("userName from userDetails inside auth endpoint is : "+userDetails.getUsername());
			
			
			if (jwtHelper.validateToken(token, userDetails)){

				System.out.println("UserDetails from auth endpoint : "+userDetails.getUsername());
				logger.info("UserDetails from auth endpoint : "+userDetails.getUsername());
				logger.info("UserName from Token inside auth endpoinit is : "+jwtHelper.getUserNameFromToken(token)); 
				logger.info("Token is valid for now");
				authResponse.setMessage("Token is valid.");
				return ResponseEntity.ok(authResponse);
			} else {
				logger.error("Token has been expired or tampered");
				authResponse.setMessage("Token is invalid or expired!!");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResponse);
			}
		}catch (ExpiredJwtException e) {
	        logger.error("Token has expired : " + e.getMessage());
	        authResponse.setMessage("Token has expired.");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResponse);
		}
        catch (CustomUserNameNotFoundException e) {
			
			authResponse.setMessage("Phone number is not registered with us!! Please register first!!");
			return ResponseEntity.badRequest().body(authResponse);
		}
		catch (NoSuchElementException e) {
			
			authResponse.setMessage("Phone number is not registered with us!! Please register!!");
			return ResponseEntity.badRequest().body(authResponse);
		}
		catch (Exception e) {
			

			logger.error("An error occured : " + e.getMessage());
			authResponse.setMessage("An error occurred : "+e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(authResponse);
		}
		
	}

	@PostMapping("/login")
	@ResponseBody
	public ResponseEntity<LogInResponse> login(@RequestBody UserLogInRequest userLogInRequest) {

		logger.info("Inside login API");
		logger.debug("login userLogInRequest : " + userLogInRequest);

		String phoneNumber = userLogInRequest.getPhoneNumber();
		String password = userLogInRequest.getPassword();


		try {
			
			accountActivationService.resetInactiveAccounts();
			
			Optional<User>optionalUser = userRepository.findByPhoneNumber(phoneNumber);
			
			User user = optionalUser.get();
			
			if(user == null) {
				
				logger.info("You're not registered with us!! Please register first!");
				
				logInResponse.setMessage("Phone number is not registered with us!! Please register first!!");
				
				return ResponseEntity.badRequest().body(logInResponse);
			
			}

              if(passwordEncoder.matches(password, user.getPassword())) {
        	
        	
			if(!user.getIsActive()) {
				
				logInResponse.setMessage("Account is currently disabled, Please try after some time.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(logInResponse);
			}
			
			 user.setFailedLoginAttempts(0);
			    user.setLastFailedLoginAttempt(null);
			    user.setIsActive(true);
			    userRepository.save(user);

		//int otp = snsService.sendOTPViaSNS(phoneNumber);
		
		int otp = twilioService.sendOtpViaTwilio(phoneNumber);
		
		if (otp == -1) {
            logger.error("Failed to send OTP via Twilio.");
            logInResponse.setMessage("Failed to send OTP.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(logInResponse);
        }

		otpStore.put(phoneNumber, otp);

		logger.info("OTP for  login : " + otp);

		logger.info("Login OTP sent successfully");

		logInResponse.setMessage("OTP Sent Successfully!!");

		return ResponseEntity.ok(logInResponse);
        	
        }else {	
			
			user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
		    user.setLastFailedLoginAttempt(LocalDateTime.now());
		    userRepository.save(user);
		    
		    
		    if (user.getFailedLoginAttempts() >= MAX_WRONG_LOGIN_ATTEMPTS) {
		        // Lock the account for 24 hours.
		       user.setIsActive(false);
		        userRepository.save(user);
		       
		        
		      String msg =   twilioService.sendAccountLockMessage(phoneNumber);
		        
		        logger.info("Your account has been locked for next 24 hours due to suspicious activivty.");
		        logger.info("Message from accountLockMessage :  "+msg);
		        logInResponse.setMessage("Account disabled for 24 hours due to suspicious activity");
		        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(logInResponse);
		        				        
		    }

			logger.info("Login OTP couldn't send, Wrong password");
			
			throw new BadCredentialsException("Wrong credentials");


		}
		}catch (BadCredentialsException e) {
	        // Handle the bad credentials exception and send an appropriate response
	        logInResponse.setMessage("Wrong Credentials!!");
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(logInResponse);
	    }
		catch (CustomUserNameNotFoundException e) {
			
			logInResponse.setMessage("Phone number is not registered with us!! Please register first!!");
			return ResponseEntity.badRequest().body(logInResponse);
		}
		catch (NoSuchElementException e) {
			
			logInResponse.setMessage("Phone number is not registered with us!! Please register!!");
			return ResponseEntity.badRequest().body(logInResponse);
		}
		catch (Exception e) {

			logger.error("Exception in login API is : " + e.getMessage());

			logInResponse.setMessage("An error occured : " + e.getMessage());

			return ResponseEntity.badRequest().body(logInResponse);

		}

		} 

	@PostMapping("/login/verify-otp")
	@ResponseBody
	public ResponseEntity<VerifyLogInOTPResponse> verifyLoginOTP(@RequestBody VerifyLogInOTPRequest verifyLogInOTPRequest
			,HttpServletResponse servletResponse) {

		logger.debug("verifyLogInOTPRequest: " + verifyLogInOTPRequest);

		try {

			Integer otp = (Integer) verifyLogInOTPRequest.getOtp();
			String phoneNumber = verifyLogInOTPRequest.getPhoneNumber();

			System.out.println("verify login otp from body: " + otp);
			System.out.println("phoneNumber from verify login otp body: " + phoneNumber);

			Integer sentOTP = otpStore.get(phoneNumber);
			
			if(sentOTP == null) {
				
				logger.info("Locally stored otp deleted. Max 3 attempts are allowed.");
				verifyLogInOTPResponse.setMessage("Maximum 3 attempts are allowed! Please try again after some time!");
				return ResponseEntity.badRequest().body(verifyLogInOTPResponse);
				
			}

			if (hasExceededMaxWrongAttempts(phoneNumber)) {
				
				wrongOtpAttempts.remove(phoneNumber);
				otpStore.remove(phoneNumber);
				logger.info("Only 3 attempts allowed!!");
				verifyLogInOTPResponse.setMessage("Maximum 3 attempts are allowed! Please try again after some time!");
				return ResponseEntity.badRequest().body(verifyLogInOTPResponse);
			}

			if (sentOTP.equals(otp)) {

				// OTP verification successful

				Optional<User>userOptional = userRepository.findByPhoneNumber(phoneNumber);
				User user = userOptional.get();

				logger.info("User verification successful for Login : " + user);

				verifyLogInOTPResponse.setMessage("Welcome to Credit Mantra!!");

				UserDTO userDTO = new UserDTO();
				
				

				userDTO.setName(user.getName());
				userDTO.setEmail(user.getEmail());
				userDTO.setPhoneNumber(user.getPhoneNumber());
				userDTO.setRole(user.getRole());

				String jwtToken = jwtHelper.generateToken(phoneNumber);
				
				String cookieValue = "token=" + jwtToken + "; Secure; SameSite=None; Max-Age=" + Duration.between(Instant.now(), expirationDate).getSeconds() + "; Path=/; Domain=backend.creditmantra.co.in; HttpOnly";
                servletResponse.setHeader("Set-Cookie", cookieValue);


				verifyLogInOTPResponse.setUserDTO(userDTO);

				otpStore.remove(phoneNumber);
				wrongOtpAttempts.remove(phoneNumber);

				return ResponseEntity.ok(verifyLogInOTPResponse);

			} else {
				logger.info("Invalid verify-login OTP");
				incrementWrongOtpAttempts(phoneNumber);

				verifyLogInOTPResponse.setMessage("Invalid OTP");
				verifyLogInOTPResponse.setUserDTO(null);

				return ResponseEntity.badRequest().body(verifyLogInOTPResponse);
			}

		} catch (Exception e) {

			logger.error("Exception in verify-login-otp is : " + e.getMessage());

			verifyLogInOTPResponse.setMessage("An error occured : " + e.getMessage());

			return ResponseEntity.badRequest().body(verifyLogInOTPResponse);
		}

	}

	private boolean hasExceededMaxWrongAttempts(String phoneNumber) {
		Integer wrongAttempts = wrongOtpAttempts.get(phoneNumber);
		return wrongAttempts != null && wrongAttempts >= MAX_WRONG_OTP_ATTEMPTS - 1;
	}

	private void incrementWrongOtpAttempts(String phoneNumber) {
		wrongOtpAttempts.compute(phoneNumber, (key, value) -> value == null ? 1 : value + 1);
	}

}
