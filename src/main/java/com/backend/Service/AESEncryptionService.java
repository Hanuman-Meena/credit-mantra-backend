package com.backend.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AESEncryptionService {
	
	private static final String AES_Algorithm = "AES";
	
	@Value("${aes.encryption_key}")
	private String encryption_key;
	
	
	public String encrypt(String plainText) throws Exception
	{
		Key encryption_key = new SecretKeySpec(this.encryption_key.getBytes(StandardCharsets.UTF_8),AES_Algorithm);
		
		Cipher cipher = Cipher.getInstance(AES_Algorithm);
		
		cipher.init(cipher.ENCRYPT_MODE, encryption_key);
		
		byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
		
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}
	
	
	public String decrypt(String encryptedText) throws Exception
	{
		Key encryption_key = new SecretKeySpec(this.encryption_key.getBytes(StandardCharsets.UTF_8), AES_Algorithm);
		
		Cipher cipher = Cipher.getInstance(AES_Algorithm);
		
		cipher.init(cipher.DECRYPT_MODE, encryption_key);
		
		byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
		
		byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
		
		return new String(decryptedBytes);
	}

}
