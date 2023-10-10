package com.backend.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VerifyLogInOTPRequest {
	
	private int otp;
	private String phoneNumber;

}
