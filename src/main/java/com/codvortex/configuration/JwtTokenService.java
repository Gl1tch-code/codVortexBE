package com.codvortex.configuration;

import com.codvortex.domain.SecretKeyEntity;
import com.codvortex.domain.User;
import com.codvortex.repository.SecretKeyRepository;
import com.codvortex.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
public class JwtTokenService {

    private final SecretKeyRepository secretKeyRepository;
    private final UserRepository userRepository;
    private String secretKey;

    public JwtTokenService(SecretKeyRepository secretKeyRepository, UserRepository userRepository) {
        this.secretKeyRepository = secretKeyRepository;
        this.userRepository = userRepository;
    }

    @PostConstruct
    private void initializeSecretKey() {
        SecretKeyEntity existing = secretKeyRepository.findById("jwt-secret").orElse(null);
        if (existing != null) {
            this.secretKey = existing.getSecret();
        } else {
            SecureRandom secureRandom = new SecureRandom();
            byte[] keyBytes = new byte[32];
            secureRandom.nextBytes(keyBytes);
            this.secretKey = Base64.getEncoder().encodeToString(keyBytes);

            secretKeyRepository.save(new SecretKeyEntity(this.secretKey));
        }
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token.substring(7));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractEmail(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token.substring(7))
                .getBody();
        return claims.getSubject();
    }


    // admin


    public boolean validateTokenAdmin(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token.substring(7));

            Optional<User> user = userRepository.findByUsernameAndAdmin(extractEmail(token));
            return user.isPresent();
        } catch (Exception e) {
            return false;
        }
    }


    // Employee


    public boolean validateTokenEmployee(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token.substring(7));

            Optional<User> user = userRepository.findByUsernameAndEmployeeOrAdmin(extractEmail(token));
            return user.isPresent();
        } catch (Exception e) {
            return false;
        }
    }

}
