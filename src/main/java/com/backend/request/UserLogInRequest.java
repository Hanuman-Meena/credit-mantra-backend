package com.backend.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserLogInRequest {
	
	private String phoneNumber;
    private String password;

}
