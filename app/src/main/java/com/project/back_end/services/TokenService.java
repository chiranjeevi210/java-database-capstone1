package com.project.back_end.services;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class TokenService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    // Injects a continuous 256-bit safe secret string from application.properties
    @Value("${jwt.secret:defaultSecretKeyForClinicManagementSystem2026SecureString}")
    private String jwtSecret;

    /**
     * Retrieves the secret cryptographic signing key required for token signatures.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a stateless JWT authorization token containing the user identifier.
     * Configures a standard token expiration window of exactly 7 days.
     */
    public String generateToken(String identifier) {
        long expirationTimeMs = 7 * 24 * 60 * 60 * 1000L; // 7 days runtime frame
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTimeMs);

        return Jwts.builder()
                .setSubject(identifier)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Decodes and extracts the user identifier string payload embedded inside the token.
     */
    public String extractIdentifier(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verifies token authenticity against explicit relational client database domains.
     */
    public boolean validateToken(String token, String user) {
        String identifier = extractIdentifier(token);
        if (identifier == null) {
            return false;
        }

        try {
            // Context branch verification routing checks
            if ("admin".equalsIgnoreCase(user)) {
                return adminRepository.findByUsername(identifier) != null;
            } else if ("doctor".equalsIgnoreCase(user)) {
                return doctorRepository.findByEmail(identifier) != null;
            } else if ("patient".equalsIgnoreCase(user) || "loggedPatient".equalsIgnoreCase(user)) {
                return patientRepository.findByEmail(identifier) != null;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
