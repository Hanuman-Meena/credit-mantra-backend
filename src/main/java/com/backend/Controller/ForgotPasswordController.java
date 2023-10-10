package com.backend.Controller;

import java.util.Map;

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
import com.backend.request.ForgotPasswordRequest;
import com.backend.request.ResetPasswordRequest;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ForgotPasswordController {
	
	private final UserRepository userRepository;
	
	private final TwilioService twilioService;
	
	private final PasswordEncoder passwordEncoder;
	
	
	@GetMapping("/forgot-password")
	@ResponseBody
	public String forgotPassword()
	{
		return "Forgot Password Form";
	}
	
	@PostMapping("/get-otp")
	public ResponseEntity<String> getOTP(@RequestBody Map<String, String> requestBody,HttpSession session)
	{
		
		String phoneNumber = requestBody.get("phoneNumber");
		
		User user = userRepository.findByPhoneNumber(phoneNumber);
		
		if(user!=null)
		{
			int otp = twilioService.sendOtpViaTwilio(phoneNumber);
			
			session.setAttribute("sentOTP", otp);
			
			session.setAttribute("phoneNumber", phoneNumber);
			
			return ResponseEntity.ok("Otp has been sent to your registered phone number");
		}
					
		return ResponseEntity.badRequest().body("This phone number is not registered with us.");
	}
	
	
	@PostMapping("verify-forgot-otp")
	public ResponseEntity<String> verifyForgotOTP(@RequestBody Map<String, Integer> requestBody, HttpSession session)
	{
		int otp = requestBody.get("otp");
		
		Integer sentOTP = (Integer)session.getAttribute("sentOTP");
		
		if(sentOTP.equals(otp))
		{				
			session.removeAttribute("sentOTP");
			
	   		return ResponseEntity.ok("OTP verified successfully!!");
								
		}			
		
		return ResponseEntity.badRequest().body("Invalid OTP");
	}
	
	
	@PostMapping("/reset-password")
	public ResponseEntity<String> resetPassword(@RequestBody @Validated ResetPasswordRequest resetPasswordRequest, HttpSession session)
	{
		
		if(resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmPassword()))
		{
			String phoneNumber = (String)session.getAttribute("phoneNumber");
			
			User user = userRepository.findByPhoneNumber(phoneNumber);
			
			try
			{
				user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
				
				userRepository.save(user);
				
				session.removeAttribute("phoneNumber");
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			return ResponseEntity.ok("Password changed successfully!!");
			
		}			
		else {
		       return ResponseEntity.badRequest().body("new passsword and confirm password doesn't macth!!");	
		}
		
	}
	

}
