package com.backend.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordVerifyOTPRequest {
	
	private String phoneNumber;
	private int otp;

}
