package com.backend.Controller;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.backend.Service.TwilioService;
import com.backend.dao.UserRepository;
import com.backend.entity.User;
import com.backend.request.UserLogInRequest;
import com.backend.request.VerifyLogInOTPRequest;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LogInController {
	
	
	private final UserRepository userRepository;
	
	private final PasswordEncoder passwordEncoder;
	
	private final TwilioService twilioService;
	
	
	@PostMapping("/login")
	@ResponseBody
	public ResponseEntity<String> login(@RequestBody UserLogInRequest userLogInRequest, HttpSession session)
	{
		
		String phoneNumber = userLogInRequest.getPhoneNumber();
		String password = userLogInRequest.getPassword();

		User user = userRepository.findByPhoneNumber(phoneNumber);
		
		if(user!= null && passwordEncoder.matches(password, user.getPassword()))
		{
			session.setAttribute("authenticatedUser", user);
			
			System.out.println("User role is: "+ user.getRole());
			
			System.out.println("Session ID: "+ session.getId());
			System.out.println("User Name: "+ user.getName());

			int otp = twilioService.sendOtpViaTwilio(phoneNumber);
			
			session.setAttribute("sentOTP", otp);
			
			return ResponseEntity.ok("OTP sent successfully!");
			
		}		
		
		return ResponseEntity.badRequest().body("Invalid phone number or password");
	}
	
	
	@PostMapping("/verify-login-otp")
	public ResponseEntity<String> verifyLoginOTP(@RequestBody VerifyLogInOTPRequest verifyLogInOTPRequest, HttpSession session)
	{		
		try {
			
			Integer sentOTP = (Integer) session.getAttribute("sentOTP");
	        Integer otp = verifyLogInOTPRequest.getOtp();	
	        User user = (User)session.getAttribute("authenticatedUser");
	        String sessionPhoneNumber = user.getPhoneNumber();
	        String phoneNumber = verifyLogInOTPRequest.getPhoneNumber();
			
	        if(sentOTP.equals(otp) && sessionPhoneNumber.equals(phoneNumber)){
	           
	        	// OTP verification successful
	 
	        	
	        	session.removeAttribute("sentOTP");
	        	
	        	System.out.println("from verify-login-otp endpoint: "+user.getName());
	            
	        	return ResponseEntity.ok("Welcome to Credit Mantra");
				}
	       
	        return ResponseEntity.badRequest().body("Invalid OTP");	
		}
		catch(Exception e) {
			 return ResponseEntity.badRequest().body("Invalid OTP");
		}
		       
	}
	
	
	    @GetMapping("/secured")
	    @ResponseBody
	    public ResponseEntity<String> getSecureData(HttpSession session) {
	    	
	        User user = (User)session.getAttribute("authenticatedUser");
	        
	        System.out.println("Session ID: "+ session.getId());

	        if (user != null) {
	        	 
	        	System.out.println("From secured endpoint: " + user.getName());
	        	 
	            return ResponseEntity.ok("This is secure data accessible only to verified and logged-in users.");
	        } 
	        
	            return ResponseEntity.status(HttpStatusCode.valueOf(401)).body("you're not authenticated!!");
	            			        
	    }
	
	}

