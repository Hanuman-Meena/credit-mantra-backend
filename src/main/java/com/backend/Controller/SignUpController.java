package com.backend.Controller;

import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.backend.Service.TwilioService;
import com.backend.dao.UserRepository;
import com.backend.entity.User;
import com.backend.request.UserRegistrationRequest;
import com.backend.securityConfig.customUserDetails;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SignUpController {

	private final UserRepository userRepository;
	
	private final PasswordEncoder passwordEncoder;
	
	private final TwilioService twilioService;

	
	@PostMapping("/signup")
	public ResponseEntity<String> registerUser(@RequestBody @Validated UserRegistrationRequest registrationRequest,
			HttpSession session) throws Exception {
		if (!registrationRequest.getPassword().equals(registrationRequest.getConfirmPassword())) {
			return ResponseEntity.badRequest().body("Password and confirm password do not match");
		}

		User user = new User();
		
		user.setRole("ROLE_USER");
		user.setName(registrationRequest.getName());
		user.setEmail(registrationRequest.getEmail());
		user.setPhoneNumber(registrationRequest.getPhoneNumber());
		user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));

		String phoneNumber = registrationRequest.getPhoneNumber();
		
		int otp = twilioService.sendOtpViaTwilio(phoneNumber);

		session.setAttribute("sentOTP", otp);
		session.setAttribute("phoneNumber", phoneNumber);


		session.setAttribute("user", user);


		return ResponseEntity.ok("Otp sent successfully!!");
	}

	@PostMapping("/verify-otp")
	public ResponseEntity<String> verifyOtp(@RequestBody Map<String, Integer> requestBody, HttpSession session) {
		
		// Retrieve the stored OTP for the user
		Integer sentOTP = (Integer) session.getAttribute("sentOTP");

		Integer otp = (Integer) requestBody.get("otp");

		User user = (User) session.getAttribute("user");
		
		if(userRepository.findByPhoneNumber(user.getPhoneNumber())!=null)
		{
			return ResponseEntity.badRequest().body("Phone number is already registered!!");
		}

		if (sentOTP.equals(otp)){
			
			userRepository.save(user);
			
			session.removeAttribute("sentOTP");
			session.removeAttribute("phoneNumber");
	
			return ResponseEntity.ok("User saved successfully!!");

		}

		return ResponseEntity.badRequest().body("Invalid OTP");

	}

}
