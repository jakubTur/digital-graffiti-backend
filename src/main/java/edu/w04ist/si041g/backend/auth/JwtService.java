package edu.w04ist.si041g.backend.auth;

import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final Duration tokenLifetime = Duration.ofHours(1); // Token lifetime of 1 hour
    public JwtService(@Value("${JWT_SECRET}") String secret) {
        if  (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("Secret length must be at least 32 characters");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    public String createToken(Long userId, String login, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + tokenLifetime.toMillis());
        return Jwts.builder()
                .subject(userId.toString())
                .claim("login", login)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public Duration getTokenLifetime() {
        return tokenLifetime;
    }
}
