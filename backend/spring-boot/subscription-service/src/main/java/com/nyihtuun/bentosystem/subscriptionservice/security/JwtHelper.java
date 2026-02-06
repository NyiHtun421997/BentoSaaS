package com.nyihtuun.bentosystem.subscriptionservice.security;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;

import static com.nyihtuun.bentosystem.subscriptionservice.security.WebSecurity.ROLE;


@Component
public class JwtHelper {

    private final SecretKey secretKey;
    private final long expirationTime;

    public JwtHelper(@Value("${jwt.secret-key}") String secretKey,
                     @Value("${jwt.expiration-time}") long expirationTime) {
        this.secretKey = getSecretKey(secretKey);
        this.expirationTime = expirationTime;
    }

    private SecretKey getSecretKey(String secretKey) {
        byte[] keyBytes = Base64.getDecoder()
                                .decode(secretKey.getBytes(StandardCharsets.UTF_8));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String subject, Collection<? extends GrantedAuthority> authorities) {
        return Jwts.builder()
                   .signWith(secretKey)
                   .claim(ROLE, authorities)
                   .subject(subject)
                   .issuedAt(new Date())
                   .expiration(new Date(System.currentTimeMillis() + expirationTime))
                   .compact();
    }

    public JwtParser getJwtParser() {
        return Jwts.parser()
                   .verifyWith(secretKey)
                   .build();
    }
}
