package com.backend.SNSConfiguration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;

@Configuration
public class SNSConfig{

	@Value("${aws.sns.accessKey}")
	private String accessKey;
	
	@Value("${aws.sns.secretKey}")
	private String secretKey;
	
	@Bean	
	public AmazonSNSClient amazonSNSClient() {
		
		BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey,secretKey);
		
		return (AmazonSNSClient) AmazonSNSClientBuilder
				.standard()
				.withRegion("ap-south-1")
				.withCredentials(new AWSStaticCredentialsProvider(credentials))
				.build();		
	}	
}
