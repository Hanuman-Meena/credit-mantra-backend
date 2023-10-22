package com.backend.registrationResponse;

import org.springframework.stereotype.Component;

import com.backend.DTO.UserDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@Data
@NoArgsConstructor
public class VerifyOTPResponse {
	
	private String message;
	
	private UserDTO userDTO;

}
