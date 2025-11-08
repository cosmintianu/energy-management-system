package org.example.user_management_microservice.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtValidationService {

    @Value("${jwt.secret}")
    private String secret;

    public Claims validateToken(String token){
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    public String extractUsername(String token){
        return validateToken(token).getSubject();
    }

    public String extractRole(String token){
        return validateToken(token).get("role").toString();
    }

    public boolean isTokenExpired(String token){
        return validateToken(token).getExpiration().before(new Date());
    }
}
