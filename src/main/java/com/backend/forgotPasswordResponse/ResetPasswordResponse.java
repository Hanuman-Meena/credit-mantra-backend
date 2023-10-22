package com.backend.forgotPasswordResponse;

import org.springframework.stereotype.Component;

import com.backend.DTO.UserDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@Data
@NoArgsConstructor
public class ResetPasswordResponse {
	
	private String message;

}
