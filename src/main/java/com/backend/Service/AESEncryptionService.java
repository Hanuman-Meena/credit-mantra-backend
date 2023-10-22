package com.backend.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AESEncryptionService {
	
	private static final String AES_Algorithm = "AES";
	
	private static final Logger logger = LoggerFactory.getLogger(AESEncryptionService.class);
	
	@Value("${aes.encryption_key}")
	private String encryption_key;
	
	
	public String encrypt(String plainText)
	{
		logger.info("Inside encrypt method of AESEncryptionService");
		
		try {
			Key encryption_key = new SecretKeySpec(this.encryption_key.getBytes(StandardCharsets.UTF_8),AES_Algorithm);
			
			Cipher cipher = Cipher.getInstance(AES_Algorithm);
			
			cipher.init(cipher.ENCRYPT_MODE, encryption_key);
			
			byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
			
			logger.info("encryptedBytes from encrypt method : "+ encryptedBytes);
			
			return Base64.getEncoder().encodeToString(encryptedBytes);
		} catch (Exception e) {
            
			logger.error("Error in encrypt method", e.getMessage());
			
			return "An error occured!!";
  
		}
	}
	
	
	public String decrypt(String encryptedText)
	{
		logger.info("Inside decrypt method of AESEncryptionService");
		
		try {
			Key encryption_key = new SecretKeySpec(this.encryption_key.getBytes(StandardCharsets.UTF_8), AES_Algorithm);
			
			Cipher cipher = Cipher.getInstance(AES_Algorithm);
			
			cipher.init(cipher.DECRYPT_MODE, encryption_key);
			
			byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
			
			byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
			
			logger.info("encyptedByted from decrypt method : "+ encryptedBytes);
			logger.info("decyptedByted from decrypt method : "+ decryptedBytes);
						
			return new String(decryptedBytes);
			
		} catch (Exception e) {
			
			logger.error("Exception in decrypt method of AESEncryptionService  : ", e.getMessage());
			
			return "An error occured!!";
			
		}
	}

}
