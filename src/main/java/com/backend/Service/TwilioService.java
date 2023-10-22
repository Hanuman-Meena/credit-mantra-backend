package com.backend.Service;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import lombok.ToString;

@Service
@ToString
public class TwilioService {

    @Value("${twilio.accountSid}")
    private String twilioAccountSid;

    @Value("${twilio.authToken}")
    private String twilioAuthToken;

    @Value("${twilio.phoneNumber}")
    private String twilioPhoneNumber;
    
    private static final Logger logger = LoggerFactory.getLogger(TwilioService.class);
    
    public String sendAccountLockMessage(String phoneNumber) {
    	
    	logger.info("Sending account lock message on user's phoneNumber");
    	
    	try {
    		Twilio.init(twilioAccountSid, twilioAuthToken);

            Message message = Message.creator(new PhoneNumber(phoneNumber), new PhoneNumber(twilioPhoneNumber), "Your account has been locked for next 24 hours due to suspicious activity. Please try to login after 24 hours.").create();

            logger.info("Account lock message sent : "+ message);
            
            return "Account lock message sent!!";
		} catch (Exception e) {
			
			logger.error("Exception in sending otp via twilio is : "+e.getMessage());
			
			return "An error occured while sending account lock message : "+e.getMessage();			
		} 
    	
    	
    }

    public int sendOtpViaTwilio(String phoneNumber) {
        
    	logger.info("Inside sendOtpViaTwilio service");
    	
    	try {
    		Twilio.init(twilioAccountSid, twilioAuthToken);

            Random random = new Random();
            int otp = random.nextInt(100000, 999999);

            Message message = Message.creator(new PhoneNumber(phoneNumber), new PhoneNumber(twilioPhoneNumber), "Your OTP is: " + otp).create();

            logger.info("otpViaTwilio is sent : "+otp);
            
            return otp;
		} catch (Exception e) {
			
			logger.error("Exception in sending otp via twilio is : "+e.getMessage());
			
			return -1;			
		}    	
    }
}
