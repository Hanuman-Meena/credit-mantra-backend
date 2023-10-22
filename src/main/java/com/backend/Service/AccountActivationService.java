package com.backend.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.backend.dao.UserRepository;
import com.backend.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountActivationService {

	@Autowired
	private UserRepository userRepository;
	
	 @Scheduled(fixedRate = 60000)
	 public void resetInactiveAccounts() {
	        // Calculate the threshold time (24 hours ago)
	        LocalDateTime thresholdTime = LocalDateTime.now().minusHours(24);
	        
	        System.out.println("Value of threshold time : "+thresholdTime);
	        
	        // Get a list of inactive users based on criteria ( isActive = false and lastFailedLoginAttempt <= thresholdTime)
	        List<User> inactiveUsers = userRepository.findInactiveUsers(thresholdTime);
	        
	        // Reset isActive status for inactive users
	        inactiveUsers.forEach(user -> {
	        	if (user.getLastFailedLoginAttempt().isBefore(thresholdTime)) {
	                user.setIsActive(true);
	                userRepository.save(user);
	            }
	        });
	    }
}
