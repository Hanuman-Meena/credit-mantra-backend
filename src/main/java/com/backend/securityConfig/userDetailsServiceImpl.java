package com.backend.securityConfig;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.backend.ExceptionHandler.CustomUserNameNotFoundException;
import com.backend.dao.UserRepository;
import com.backend.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class userDetailsServiceImpl implements UserDetailsService{
	
	
	@Autowired
	private UserRepository userRepository;
	

	@Override
	public UserDetails loadUserByUsername(String username) throws CustomUserNameNotFoundException {
		
		
		//fetching user from the database
				
		Optional<User> user = userRepository.findByPhoneNumber(username);
		
		if(user == null) {
			throw new NoSuchElementException("No value present");
		}
		  
        // Converting userDetail to UserDetails 
        return user.map(customUserDetails::new) 
                .orElseThrow(() -> new UsernameNotFoundException("This phone number is not registered with us!! Please register!! " )); 
		
	}

}
