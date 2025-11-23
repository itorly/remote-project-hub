package com.itorly.rph.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // For demo purposes only. In real life, load from config/env.
    private final Key key = Keys.hmacShaKeyFor(
            "replace-with-a-long-secret-key-at-least-32-bytes".getBytes()
    );

    // e.g. 24 hours
    private final long validityInMillis = 24 * 60 * 60 * 1000L;

    public String generateToken(Long userId, String email) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(validityInMillis);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> validateAndParseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = validateAndParseToken(token).getBody();
        return Long.valueOf(claims.getSubject());
    }

    public String getEmailFromToken(String token) {
        Claims claims = validateAndParseToken(token).getBody();
        return claims.get("email", String.class);
    }
}
