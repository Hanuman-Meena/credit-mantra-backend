package com.backend.Service;

import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class TwilioService {

    @Value("${twilio.accountSid}")
    private String twilioAccountSid;

    @Value("${twilio.authToken}")
    private String twilioAuthToken;

    @Value("${twilio.phoneNumber}")
    private String twilioPhoneNumber;

    public int sendOtpViaTwilio(String phoneNumber) {
        Twilio.init(twilioAccountSid, twilioAuthToken);

        Random random = new Random();
        int otp = random.nextInt(100000, 999999);

        Message message = Message.creator(new PhoneNumber(phoneNumber), new PhoneNumber(twilioPhoneNumber), "Your OTP is: " + otp).create();

        return otp;
    }
}
