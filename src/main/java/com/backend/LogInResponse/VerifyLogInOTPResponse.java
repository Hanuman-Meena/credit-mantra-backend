package com.backend.LogInResponse;

import org.springframework.stereotype.Component;

import com.backend.DTO.UserDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Component
public class VerifyLogInOTPResponse {
	
	private String message;
	
	private UserDTO userDTO;

}
