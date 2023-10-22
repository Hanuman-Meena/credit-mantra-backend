package com.backend.Service;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class SnsService {

    private final AmazonSNS amazonSNS;
    private static final Logger logger = LoggerFactory.getLogger(SnsService.class);

    public int sendOTPViaSNS(String phoneNumber) {
        try {
            int otp = generateRandomOtp();
            String message = "Your OTP is: " + otp;
            PublishRequest request = new PublishRequest()
                    .withMessage(message)
                    .withPhoneNumber(phoneNumber);
            PublishResult result = amazonSNS.publish(request);
            
            logger.info("Request from AWS SNS : "+request);
            logger.info("Result from AWS SNS : "+result);
            logger.info("OTP sent successfully: " + otp);
            return otp;
        } catch (Exception e) {
            logger.error("Failed to send OTP via AWS SNS: " + e.getMessage());
            return -1; // Return an error code to indicate OTP sending failure
        }
    }

    private int generateRandomOtp() {
        // Generate a random 6-digit OTP
        Random random = new Random();
        int min = 100000; // Minimum 6-digit number
        int max = 999999; // Maximum 6-digit number
        return random.nextInt(max - min + 1) + min;
    }
}
