package com.backend.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRegistrationRequest {
	
	private String name;
    private String email;
    private String phoneNumber;
    private String password;
    private String confirmPassword;

}
