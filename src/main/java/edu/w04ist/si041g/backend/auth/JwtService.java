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
    private final Duration accessTokenLifetime = Duration.ofMinutes(20); // Token lifetime of 1 hour
    private final Duration refreshTokenLifetime = Duration.ofDays(30); // Token lifetime of 1 hour
    public JwtService(@Value("${JWT_SECRET}") String secret) {
        if  (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("Secret length must be at least 32 characters");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, String login, String role) {
        return createToken(userId, login, role,"access", accessTokenLifetime);
    }
    public String createRefreshToken(Long userId) {
        return createToken(userId, null, null, "refresh", refreshTokenLifetime);
    }
    public String createToken(Long userId, String login, String role, String type, Duration lifetime) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", type)
                .claim("login", login)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + lifetime.toMillis()))
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
    public boolean isAccessToken(Claims claims) {
        return "access".equals(claims.get("type", String.class));
    }

    public boolean isRefreshToken(Claims claims) {
        return "refresh".equals(claims.get("type", String.class));
    }
    public Duration getAccessTokenLifetime() {
        return accessTokenLifetime;
    }
}
