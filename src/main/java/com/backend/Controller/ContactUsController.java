package com.backend.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.backend.LogInResponse.ContactUsResponse;
import com.backend.Service.EmailService;
import com.backend.request.ContactUsRequest;
import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class ContactUsController {
	
	@Autowired
	private final EmailService emailService;
	
	private final ContactUsResponse contactUsResponse;
	
	Logger logger = LoggerFactory.getLogger(ContactUsController.class);
	
	
	@PostMapping("/contact")
	@ResponseBody
	public ResponseEntity<ContactUsResponse> contactUs(@RequestBody ContactUsRequest contactUsRequest) {
		
		logger.info("Inside contact Us method of contactUsController");
		
		logger.info("Email service : "+this.emailService);
		logger.info("Contact Us Form Details : "+ contactUsRequest);
		
		
	
		String subject = "Enquiry for " + contactUsRequest.getChosenService() ;
		
		String message = " "
				+"<div>"
                + "<h3>User Details</h3>"
                + "<p><strong>First Name:</strong> " + contactUsRequest.getFirstName() + "</p>"
                + "<p><strong>Last Name:</strong> " + contactUsRequest.getLastName() + "</p>"
                + "<p><strong>Date of Birth:</strong> " + contactUsRequest.getDob() + "</p>"
                + "<p><strong>Phone Number:</strong> " + contactUsRequest.getPhoneNumber() + "</p>"
                + "<p><strong>Email:</strong> " + contactUsRequest.getEmail() + "</p>"
                + "<p><strong>Employment Type:</strong> " +contactUsRequest.getEmploymentType() + "</p>"
                + "<p><strong>Chosen Service:</strong> " + contactUsRequest.getChosenService() + "</p>"
                + "</div>";
		
		logger.info(message);
		
		try {
			boolean flag = this.emailService.sendEmail(subject, message, "info@creditmantra.co.in");
			
			logger.info(message);
			
			if(flag)
			{
				logger.info("Contact Us form submitted successfully!!");
				
				contactUsResponse.setMessage("Submitted Successfully!!");
				
				return ResponseEntity.ok(contactUsResponse);
			}
			
			else {
				
				logger.info("Can't send email to this address");
				
				contactUsResponse.setMessage("Can't send email to this address");
				
				return ResponseEntity.badRequest().body(contactUsResponse);
			}
		} catch (Exception e) {
			
			logger.error("An error occured "+e.getMessage());
			
			contactUsResponse.setMessage("An error occured :"+e.getMessage());
			
			return ResponseEntity.badRequest().body(contactUsResponse);
		}
		
	}

}
