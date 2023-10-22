package com.backend.Service;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.ToString;

@Service
@ToString
public class EmailService {

	@Value("${spring.mail.host}")
	private String host;

	@Value("${spring.mail.port}")
	private String port;

	@Value("${spring.mail.username}")
	private String username;

	@Value("${spring.mail.password}")
	private String password;

	@Value("${spring.mail.properties.mail.smtp.auth}")
	private String smtpAuth;

	@Value("${spring.mail.properties.mail.smtp.starttls.enable}")
	private String smtpStarttls;

	@Value("${spring.mail.properties.mail.smtp.ssl.enable}")
	private String smtpSslEnable;
	
	Logger logger = LoggerFactory.getLogger(EmailService.class);

	public boolean sendEmail(String subject, String message, String to) {
		
		logger.info("Inside sendEmail method of EmailService");

		boolean f = false;

		Properties properties = System.getProperties();

		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", port);
		properties.put("mail.smtp.auth", smtpAuth);
		properties.put("mail.smtp.starttls.enable", smtpStarttls);
		properties.put("mail.smtp.ssl.enable", smtpSslEnable);

		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {

				return new PasswordAuthentication(username, password);
			}

		});
		

		MimeMessage mimeMessage = new MimeMessage(session);

		try {
			
			mimeMessage.setFrom(username);
			mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			mimeMessage.setSubject(subject);
			mimeMessage.setContent(message,"text/html");
			
			
		    Transport.send(mimeMessage);
			
			logger.info("mimeMessage");
			
			f = true;
			
			return f;
												
	}catch(	Exception e)
	{		
		logger.error("An error occured : "+e.getMessage());
		
		e.printStackTrace();
		
		return f;
					
		}

}

}
