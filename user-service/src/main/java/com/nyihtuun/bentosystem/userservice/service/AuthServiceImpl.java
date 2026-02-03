package com.nyihtuun.bentosystem.userservice.service;

import com.nyihtuun.bentosystem.userservice.security.WebSecurity;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.nyihtuun.bentosystem.userservice.security.WebSecurity.AUTHORITY;
import static com.nyihtuun.bentosystem.userservice.security.WebSecurity.ROLE;

@AllArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final JwtHelper jwtHelper;

    @Override
    public boolean validateToken(String token) {
        String subject = jwtHelper.getJwtParser()
                                  .parseSignedClaims(token)
                                  .getPayload()
                                  .getSubject();

        if (subject == null) {
            return false;
        }

        Date expirationDate = jwtHelper.getJwtParser()
                                       .parseSignedClaims(token)
                                       .getPayload()
                                       .getExpiration();

        return new Date().before(expirationDate);
    }

    @Override
    public String generateToken(String userId, Collection<? extends GrantedAuthority> authorities) {
        return jwtHelper.generateToken(userId, authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(String token) {
        Claims claims = jwtHelper.getJwtParser()
                                 .parseSignedClaims(token)
                                 .getPayload();
        List<Map<String, String>> roles = claims.get(ROLE, List.class);
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.get(AUTHORITY)))
                .toList();
    }

    @Override
    public String getSubject(String token) {
        return jwtHelper.getJwtParser()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
