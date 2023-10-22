package com.backend.JWTConfig;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.backend.securityConfig.userDetailsServiceImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtHelper {
	
	public static final long JWT_TOKEN_VALIDITY = 10*60*60*24;
	
	private static final Logger logger = LoggerFactory.getLogger(JwtHelper.class);
	
	@Autowired
	private userDetailsServiceImpl userDetailsService;
	
	
	@Value("${security.jwt.secret}")
	private String secret;
	
	public String test() {
		logger.info("testing");
		return "testing";
	}

	public String getUserNameFromToken(String token) {
		
		logger.info("getUserNameFromToken");
		return getClaimFromToken(token,Claims::getSubject);
	}
	
	public Date getExpirationDateFromToken(String token) {
		
		logger.info("getExpirationDateFromToken");
		return getClaimFromToken(token,Claims::getExpiration);
	}
	
	public <T> T getClaimFromToken(String token,Function<Claims,T> claimsResolver) {
		
		logger.info("getClaimFromToken");
		try {
            final Claims claims = getAllClaimsFromToken(token);
            logger.info("Claims from token : " + claims);
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            
            logger.error("Error getting claim from token: " + e.getMessage());
            return null;
        }
	}
	
	private Claims getAllClaimsFromToken(String token) {
		
		logger.info("getAllClaimsFromToken");
		try {
            return Jwts.parser().setSigningKey(getSigningKey()).parseClaimsJws(token).getBody();
        
        } catch (Exception e) {
            // Handle other exceptions
            logger.error("General error parsing token: " + e.getMessage());
            throw new RuntimeException("Error parsing token");
        }
	}
	
	private Boolean isTokenExpired(String token) {
		
		logger.info("isTokenExpired");
		try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        }catch (ExpiredJwtException e) {
            // Handle expired token exception
            logger.error("Token expired: " + e.getMessage());
            return true; // Token is expired
        }
		catch (Exception e) {
            
            logger.error("Error checking token expiration: " + e.getMessage());
            return true; 
        }
	}
	
	public String generateToken(String username) {
		
		logger.info("generateToken");
		Map<String,Object> claims = new HashMap<>();
		return doGenerateToken(claims,username);
	}
	
	private String doGenerateToken(Map<String,Object>claims, String username) {
		
		logger.info("doGenerateToken");
		
		try {
            return Jwts.builder().setClaims(claims).setSubject(username)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
        } catch (Exception e) {
            // Handle exception when generating the token
            logger.error("Error generating token: " + e.getMessage());
            throw new RuntimeException("Error generating token");
        }
	}
	
	public Boolean validateToken(String token,UserDetails userDetails) {
		
		logger.info("validateToken");
		final String username = getUserNameFromToken(token);
		System.out.println("getUserName userDetails inside Validate Token : "+userDetails.getUsername());
		System.out.println("getUserNameFromToken inside ValidateToken : "+username);
		return (username.equals(userDetails.getUsername()) &&!isTokenExpired(token));
	}
	
	public Key getSigningKey() {
		
		logger.info("getSigningKey");
		logger.info("Secret from app.properties : "+secret);
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
	
	
	
}
