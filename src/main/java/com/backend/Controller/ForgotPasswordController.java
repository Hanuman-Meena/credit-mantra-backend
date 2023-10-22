package com.backend.Controller;

import java.util.Map;
import java.util.NoSuchElementException;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.backend.Service.SnsService;
import com.backend.Service.TwilioService;
import com.backend.dao.UserRepository;
import com.backend.entity.User;
import com.backend.forgotPasswordResponse.ForgotPasswordOTPResponse;
import com.backend.forgotPasswordResponse.ForgotPasswordResponse;
import com.backend.forgotPasswordResponse.ResetPasswordResponse;
import com.backend.request.ForgotPasswordVerifyOTPRequest;
import com.backend.request.ResetPasswordRequest;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ForgotPasswordController {

	private final UserRepository userRepository;

	private final TwilioService twilioService;

	//private final SnsService snsService;

	private final PasswordEncoder passwordEncoder;

	private final ForgotPasswordResponse forgotPasswordResponse;

	private final ForgotPasswordOTPResponse forgotPasswordOTPResponse;

	private final ResetPasswordResponse resetPasswordResponse;

	private ConcurrentHashMap<String, Integer> storeOTP = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, String> storePhoneNumber = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, Boolean> isVerifiedMap = new ConcurrentHashMap<>();
	private Map<String, Integer> wrongOtpAttempts = new ConcurrentHashMap<>();

	private static final int MAX_WRONG_OTP_ATTEMPTS = 3;

	private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordController.class);

	@PostMapping("/forgot-password")
	@ResponseBody
	public ResponseEntity<ForgotPasswordResponse> getOTP(@RequestBody Map<String, String> requestBody) {

		String phoneNumber = requestBody.get("phoneNumber");

		logger.info("Inside forgot-password get-otp");
		logger.debug("ForgotPassword get-otp RequestBody : " + requestBody);

		try {

			Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
			User user = userOptional.get();

			logger.info("User from forgot-password is : " + user);

			if (user != null) {
				// int otp = snsService.sendOTPViaSNS(phoneNumber);

				int otp = twilioService.sendOtpViaTwilio(phoneNumber);

				if (otp == -1) {
					logger.error("Failed to send OTP via Twilio.");
					forgotPasswordResponse.setMessage("Failed to send OTP.");
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(forgotPasswordResponse);
				}

				storeOTP.put(phoneNumber, otp);
				storePhoneNumber.put(phoneNumber, phoneNumber);

				logger.info("OTP has been sent for forgot-password " + otp);

				forgotPasswordResponse.setMessage("Otp has been sent on your registered number");

				return ResponseEntity.ok(forgotPasswordResponse);
			} else {

				logger.info("Phone number for forgot password get-otp is not registered!!");

				forgotPasswordResponse.setMessage("This phone number is not registered with us");

				return ResponseEntity.badRequest().body(forgotPasswordResponse);

			}
		}catch (NoSuchElementException e) {
			logger.error("NoSuchElementException in forgot-password : "+e.getMessage());
			forgotPasswordResponse.setMessage("Phone number is not registered with us!! Please register!!");
			return ResponseEntity.badRequest().body(forgotPasswordResponse);
		} 
		catch (Exception e) {

			logger.error("An error occured : " + e.getMessage());

			forgotPasswordResponse.setMessage("An error occured : " + e.getMessage());

			return ResponseEntity.badRequest().body(forgotPasswordResponse);
		}
	}

	@PostMapping("/forgot/verify-otp")
	@ResponseBody
	public ResponseEntity<ForgotPasswordOTPResponse> verifyForgotOTP(
			@RequestBody ForgotPasswordVerifyOTPRequest forgotPasswordVerifyOTPRequest) {

		String phoneNumber = forgotPasswordVerifyOTPRequest.getPhoneNumber();
		Integer otp = (Integer) forgotPasswordVerifyOTPRequest.getOtp();

		logger.info("Inside verify-forgot-otp API");
		logger.debug("Verify-Forgot-OTP RequestBody :  " + otp);

		try {

			Integer sentOTP = (Integer) storeOTP.get(phoneNumber);
			String storedPhoneNumber = storePhoneNumber.get(phoneNumber);

			if (sentOTP == null || storedPhoneNumber == null) {

				logger.info("Locally stored otp deleted. Max 3 attempts are allowed.");
				forgotPasswordOTPResponse.setMessage("Maximum 3 attempts are allowed! Please try again after some time!");
				return ResponseEntity.badRequest().body(forgotPasswordOTPResponse);

			}

			if (hasExceededMaxWrongAttempts(phoneNumber)) {

				wrongOtpAttempts.remove(phoneNumber);
				storeOTP.remove(phoneNumber);
				storePhoneNumber.remove(phoneNumber);
				logger.info("Only 3 attempts allowed!!");
				forgotPasswordOTPResponse
						.setMessage("Maximum 3 attempts are allowed! Please try again after some time!");
				return ResponseEntity.badRequest().body(forgotPasswordOTPResponse);
			}

			if (sentOTP != null && sentOTP.equals(otp) && storedPhoneNumber != null
					&& storedPhoneNumber.equals(phoneNumber)) {

				logger.info("forgot-password-otp verified");

				isVerifiedMap.put(phoneNumber, true);

				wrongOtpAttempts.remove(phoneNumber);
				storeOTP.remove(phoneNumber);

				forgotPasswordOTPResponse.setMessage("OTP verification successful");

				return ResponseEntity.ok(forgotPasswordOTPResponse);

			} else {

				logger.info("invalid forgot-password otp, couldn't verify");
				incrementWrongOtpAttempts(phoneNumber);

				forgotPasswordOTPResponse.setMessage("Invalid OTP");

				return ResponseEntity.badRequest().body(forgotPasswordOTPResponse);
			}
		} catch (Exception e) {

			logger.error("Exception in verify-forgot-otp is : " + e.getMessage());

			forgotPasswordOTPResponse.setMessage("An error occured : " + e.getMessage());

			return ResponseEntity.badRequest().body(forgotPasswordOTPResponse);
		}
	}

	@PostMapping("/reset-password")
	@ResponseBody
	public ResponseEntity<ResetPasswordResponse> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {

		logger.info("Inside reset-password API");
		logger.debug("ResetPassword requestBody : " + resetPasswordRequest);

		try {
			if (resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmPassword())) {

				String phoneNumber = resetPasswordRequest.getPhoneNumber();

				Boolean isVerified = isVerifiedMap.get(phoneNumber);

				if (isVerified != null && isVerified) {
					Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
					User user = userOptional.get();

					if (user == null) {
						logger.info("User not found for password reset");
						resetPasswordResponse.setMessage("User not found for password reset");
						return ResponseEntity.badRequest().body(resetPasswordResponse);
					}

					logger.info("User from reset-password is : " + user);

					user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));

					userRepository.save(user);

					//storeOTP.remove(phoneNumber);
					storePhoneNumber.remove(phoneNumber);
					isVerifiedMap.remove(phoneNumber);

					logger.info("reset password successful");

					resetPasswordResponse.setMessage("Password reset successful");

					return ResponseEntity.ok(resetPasswordResponse);

				} else {
					logger.info("Password reset failed, OTP is not verified");
					resetPasswordResponse.setMessage("OTP verification is required for password reset");
					return ResponseEntity.badRequest().body(resetPasswordResponse);
				}
			} else {
				logger.info("Reset password failed, new password and  confirm password doesn't match");

				resetPasswordResponse.setMessage("new password and confirm password don't macth");

				return ResponseEntity.badRequest().body(resetPasswordResponse);
			}
		} catch (Exception e) {

			logger.error("Exception in reset password is : " + e.getMessage());

			resetPasswordResponse.setMessage("An error occured : " + e.getMessage());

			return ResponseEntity.badRequest().body(resetPasswordResponse);
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
