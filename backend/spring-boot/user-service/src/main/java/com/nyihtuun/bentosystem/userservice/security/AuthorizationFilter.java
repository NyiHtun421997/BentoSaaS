package com.nyihtuun.bentosystem.userservice.security;

import com.nyihtuun.bentosystem.userservice.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.nyihtuun.bentosystem.userservice.security.WebSecurity.BEARER_PREFIX;

@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {

    private final AuthService authService;

    public AuthorizationFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req,
                                    @NonNull HttpServletResponse res,
                                    @NonNull FilterChain chain) throws IOException, ServletException {

        log.info("Authorization filter invoked.");
        String authorizationHeader = req.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            log.info("No authorization header found. Move on to the next filter.");
            chain.doFilter(req, res);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(authorizationHeader);
        if (authentication == null) {
            log.warn("Invalid token. Moving on to the next filter.");
            chain.doFilter(req, res);
            return;
        }
        log.info("Successfully generated authentication token.");

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("Authentication token set in SecurityContextHolder.");

        chain.doFilter(req, res);
        log.info("Authorization filter completed.");
    }

    private UsernamePasswordAuthenticationToken getAuthentication(String authorizationHeader) {
        String token = authorizationHeader.substring(BEARER_PREFIX.length());
        if (!authService.validateToken(token)) return null;

        String subject = authService.getSubject(token);

        return new UsernamePasswordAuthenticationToken(subject, null, authService.getAuthorities(token));
    }
}
