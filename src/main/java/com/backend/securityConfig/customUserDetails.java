package com.backend.securityConfig;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.backend.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class customUserDetails implements UserDetails {

	
	private String phoneNumber;
	private String password;
	
	private List<GrantedAuthority> authorities;
	
	public customUserDetails (User user) {
		phoneNumber = user.getPhoneNumber();
		password = user.getPassword();
		authorities = Arrays.stream(user.getRole().split(",")) 
                .map(SimpleGrantedAuthority::new) 
                .collect(Collectors.toList()); 
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		
		return authorities;
	}

	@Override
	public String getPassword() {
		
		return password;
	}

	@Override
	public String getUsername() {
		
		return phoneNumber;
	}

	@Override
	public boolean isAccountNonExpired() {
		
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		
		return true;
	}

	@Override
	public boolean isEnabled() {
		
	    return true;
	}
	

}
