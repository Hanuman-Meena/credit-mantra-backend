package com.backend.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ContactUsRequest {
	
	private String firstName;
	
	private String lastName;
	
	private String dob;
	
	private String phoneNumber;
	
	private String email;
	
	private String employmentType;
	
	private String chosenService;
	
	
	

}
