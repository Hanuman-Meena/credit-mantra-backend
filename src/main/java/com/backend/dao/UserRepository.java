package com.backend.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import com.backend.entity.User;

@Component
public interface UserRepository extends JpaRepository<User, Long> {
	
	@Query("select u from User u where u.phoneNumber =:phoneNumber")
	Optional<User> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);
	
	 @Query("SELECT u FROM User u WHERE u.isActive = false AND u.lastFailedLoginAttempt <=:thresholdTime")
	    List<User> findInactiveUsers(@Param("thresholdTime") LocalDateTime thresholdTime);
	

    
}