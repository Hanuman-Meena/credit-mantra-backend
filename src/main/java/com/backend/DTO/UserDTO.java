package com.backend.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {
	
	    private String name;
	    private String phoneNumber;
	    private String email;
	    private String role;

}
