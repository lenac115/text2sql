package com.ai.main.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey secretKey;
    private final long accessExpiration;
    private final long refreshExpiration;
    private final StringRedisTemplate redisTemplate;


    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration,
            StringRedisTemplate redisTemplate
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
        this.redisTemplate = redisTemplate;
    }

    public String generateAccessToken(String email, String role) {
        return buildToken(email, role, TYPE_ACCESS, accessExpiration);
    }

    public String generateRefreshToken(String email, String role) {
        return buildToken(email, role, TYPE_REFRESH, refreshExpiration);
    }

    public long getRefreshExpirationMs() {
        return refreshExpiration;
    }

    private String buildToken(String email, String role, String type, long expiration) {
        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_TYPE, type)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public Long getExpFromToken(Authentication authentication) {
        String token = authentication.getCredentials().toString();
        Claims claims = parseClaims(token);
        Date exp = claims.getExpiration();

        long now = System.currentTimeMillis();

        long diffMillis = exp.getTime() - now;
        long seconds = diffMillis / 1000;

        return Math.max(0, seconds);
    }

    public boolean isAccessToken(String token) {
        try {
            return TYPE_ACCESS.equals(parseClaims(token).get(CLAIM_TYPE, String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            return TYPE_REFRESH.equals(parseClaims(token).get(CLAIM_TYPE, String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return !Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}