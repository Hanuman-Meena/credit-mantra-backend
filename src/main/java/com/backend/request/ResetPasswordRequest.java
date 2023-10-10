package com.backend.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResetPasswordRequest {
	
	private String newPassword;
	
	private String confirmPassword;

}
