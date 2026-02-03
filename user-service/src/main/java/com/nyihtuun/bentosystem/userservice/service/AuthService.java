package com.nyihtuun.bentosystem.userservice.service;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface AuthService {
    boolean validateToken(String token);
    String generateToken(String userId, Collection<? extends GrantedAuthority> authorities);
    Collection<? extends GrantedAuthority> getAuthorities(String token);
    String getSubject(String token);
}
