package com.backend.Controller;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.backend.DTO.UserDTO;
import com.backend.JWTConfig.JwtHelper;
import com.backend.Service.TwilioService;
import com.backend.dao.UserRepository;
import com.backend.entity.User;
import com.backend.registrationResponse.RegistrationResponse;
import com.backend.registrationResponse.VerifyOTPResponse;
import com.backend.request.UserRegistrationRequest;
import com.backend.request.VerifySignUpOTPRequest;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SignUpController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtHelper jwtHelper;
    private final RegistrationResponse response;
    private final TwilioService twilioService;
    private final VerifyOTPResponse verifyOTPResponse;
    //private final SnsService snsService;
    
    private Map<String, User> userStorage = new ConcurrentHashMap<>();
    private Map<String, Integer> otpStorage = new ConcurrentHashMap<>();
    private Map<String, Integer> wrongOtpAttempts = new ConcurrentHashMap<>();
    private static final int MAX_WRONG_OTP_ATTEMPTS = 3;
    
    Instant expirationDate = Instant.now().plus(Duration.ofDays(12));
    
    
    private static final Logger logger = LoggerFactory.getLogger(SignUpController.class);


    @PostMapping("/signup")
    public ResponseEntity<RegistrationResponse> registerUser(@RequestBody UserRegistrationRequest registrationRequest) {
        try {
            if (!registrationRequest.getPassword().equals(registrationRequest.getConfirmPassword())) {
                response.setMessage("Password and confirm password do not match");
                return ResponseEntity.badRequest().body(response);
            }
            
            String phoneNumber = registrationRequest.getPhoneNumber();
            
            Optional<User> existingUser = userRepository.findByPhoneNumber(phoneNumber);
        	
        	if (existingUser.isPresent()) {
        		logger.info("Phone number is already registered!!");
                response.setMessage("Phone number is already registered. Please log in.");
                return ResponseEntity.badRequest().body(response);
            }

            User user = new User();
            user.setRole("ROLE_USER");
            user.setName(registrationRequest.getName());
            user.setEmail(registrationRequest.getEmail());
            user.setPhoneNumber(registrationRequest.getPhoneNumber());
            user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
            user.setIsActive(true);

           // int otp = snsService.sendOTPViaSNS(phoneNumber);
            
            int otp = twilioService.sendOtpViaTwilio(phoneNumber);
            
            if (otp == -1) {
                logger.error("Failed to send OTP via Twilio.");
                response.setMessage("Failed to send OTP.");
           
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
            otpStorage.put(phoneNumber, otp);
            userStorage.put(phoneNumber,user);
            
            System.out.println("signup controller is working fine as of now");
 

            response.setMessage("OTP sent successfully!!");
           

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage("An error occurred : " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/signup/verify-otp")
    public ResponseEntity<VerifyOTPResponse> verifyOtp(@RequestBody VerifySignUpOTPRequest verifySignUpOTPRequest, HttpServletResponse servletResponse) {
       
    	
    	String phoneNumber = verifySignUpOTPRequest.getPhoneNumber();
    	Integer otp = (Integer)verifySignUpOTPRequest.getOtp();
    	
    	User user = userStorage.get(phoneNumber);
    	
    	Integer sentOTP = otpStorage.get(phoneNumber);
    	
    	try {
    		
    		if (user == null) {
                logger.info("Locally stored user deleted, Maximum 3 attempts are allowed.");
                verifyOTPResponse.setMessage("Maximum 3 attempts are allowed! Please try again after some time!");
                return ResponseEntity.badRequest().body(verifyOTPResponse);
            }
        	
        	
        	 if (hasExceededMaxWrongAttempts(phoneNumber)) {
        		 
        		 wrongOtpAttempts.remove(phoneNumber);
        		 userStorage.remove(phoneNumber);
        		 otpStorage.remove(phoneNumber);
                 logger.info("Only 3 attempts allowed!!");
                 verifyOTPResponse.setMessage("Maximum 3 attempts are allowed! Please try again after some time!");
                 return ResponseEntity.badRequest().body(verifyOTPResponse);
             }
        	 
        	 if (sentOTP.equals(otp)) {
                 userRepository.save(user);
                 userStorage.remove(phoneNumber);
                 otpStorage.remove(phoneNumber);
                 wrongOtpAttempts.remove(phoneNumber);
                 
                 String jwtToken =  jwtHelper.generateToken(phoneNumber);
                 
               
                 String cookieValue = "token=" + jwtToken + "; Secure; SameSite=None; Max-Age=" + Duration.between(Instant.now(), expirationDate).getSeconds() + "; Path=/; Domain=backend.creditmantra.co.in; HttpOnly";
                 servletResponse.setHeader("Set-Cookie", cookieValue);
             
                 UserDTO userDTO = new UserDTO();
                 userDTO.setName(user.getName());
                 userDTO.setEmail(user.getEmail());
                 userDTO.setPhoneNumber(phoneNumber);
                 userDTO.setRole(user.getRole());
                 
                 verifyOTPResponse.setMessage("Signup successful");
                 verifyOTPResponse.setUserDTO(userDTO);

                 return ResponseEntity.ok(verifyOTPResponse);
             } else {
                 // Increment the wrong OTP attempts
                 incrementWrongOtpAttempts(phoneNumber);
                 logger.info("Invalid OTP");
                 verifyOTPResponse.setMessage("Invalid OTP. Please try again.");
                 return ResponseEntity.badRequest().body(verifyOTPResponse);
             }
        			
		} catch (Exception e) {
			
			logger.error("An error occured : "+e.getMessage());
            verifyOTPResponse.setMessage("An error occurred: " + e.getMessage());
            return ResponseEntity.badRequest().body(verifyOTPResponse);
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
