package com.nyihtuun.bentosystem.invoiceservice.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface AuthService {
    boolean validateToken(String token);
    Collection<? extends GrantedAuthority> getAuthorities(String token);
    String getSubject(String token);
}
