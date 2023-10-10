package com.backend.securityConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.backend.dao.UserRepository;
import com.backend.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class userDetailsServiceImpl implements UserDetailsService{
	
	
	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		
		//fetching user from the database
		
		User user = userRepository.findByPhoneNumber(username);
		
		if(user == null)
		{
			throw new UsernameNotFoundException("Couldn't find user!!");
		}
		
		customUserDetails customUserDetail = new customUserDetails(user);
		
		return customUserDetail;
		
	}

}
