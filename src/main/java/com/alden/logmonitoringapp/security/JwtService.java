package com.alden.logmonitoringapp.security;

// JWT token generation and validation using jjwt 0.12.x
// https://github.com/jwtk/jjwt

import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;


@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;


    // @Value reads from application.properties, then passes it into constructor, thus avoiding hardcoding the secret
    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs) {
                                            // make it UTF-8 so a non-ASCII secret always encodes to the same bytes.
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // catches all jjwt exceptions
    public boolean isTokenValid(String token) {
        try {
            Date expiry = parseClaims(token).getExpiration();
            return expiry.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // from the jjwt README "Read a JWT" example
    // Jwts.parser() is the 0.12.x API — the old parserBuilder() was removed
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }
}

