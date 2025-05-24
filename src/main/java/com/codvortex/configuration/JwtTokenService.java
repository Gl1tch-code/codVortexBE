package com.codvortex.configuration;

import com.codvortex.domain.SecretKeyEntity;
import com.codvortex.repository.SecretKeyRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class JwtTokenService {

    private final SecretKeyRepository secretKeyRepository;
    private String secretKey;

    public JwtTokenService(SecretKeyRepository secretKeyRepository) {
        this.secretKeyRepository = secretKeyRepository;
    }

    @PostConstruct
    private void initializeSecretKey() {
        SecretKeyEntity existing = secretKeyRepository.findById("jwt-secret").orElse(null);
        if (existing != null) {
            this.secretKey = existing.getSecret();
        } else {
            // Generate a 256-bit (32-byte) secure random key and base64 encode it
            SecureRandom secureRandom = new SecureRandom();
            byte[] keyBytes = new byte[32];
            secureRandom.nextBytes(keyBytes);
            this.secretKey = Base64.getEncoder().encodeToString(keyBytes);

            secretKeyRepository.save(new SecretKeyEntity(this.secretKey));
        }
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractEmail(String token) {
        Claims claims = Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }
}
