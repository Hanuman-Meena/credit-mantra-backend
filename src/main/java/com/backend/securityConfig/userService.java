package com.backend.securityConfig;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.backend.dao.UserRepository;
import com.backend.entity.User;

@Service
public class userService {

	
	@Autowired
	private UserRepository userRepository;
	
	public List<User> getUser()
	{
		return userRepository.findAll();
		
	}
}
