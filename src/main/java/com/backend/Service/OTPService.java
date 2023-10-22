package com.backend.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class OTPService {
   
    private final Map<String, Integer> otpMap = new ConcurrentHashMap<>();

   
    public void storeOTP(String phoneNumber, int otp) {
        otpMap.put(phoneNumber, otp);
    }

    // Retrieve the stored OTP for a given phone number
    public int getStoredOTP(String phoneNumber) {
        return otpMap.getOrDefault(phoneNumber, -1); // Return -1 if OTP doesn't exist 
    }
}

