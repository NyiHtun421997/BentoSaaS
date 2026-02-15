package com.nyihtuun.bentosystem.userservice.security;

import com.nyihtuun.bentosystem.userservice.security.principal.UserPrincipal;
import com.nyihtuun.bentosystem.userservice.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Objects;

import static com.nyihtuun.bentosystem.userservice.security.WebSecurity.BEARER_PREFIX;

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthService authService;
    private final Environment environment;

    public AuthenticationFilter(AuthenticationManager authenticationManager,
                                AuthService authService, Environment environment) {
        super(authenticationManager);
        this.authService = authService;
        this.environment = environment;
    }

    @Override
    protected void successfulAuthentication(
            @NonNull HttpServletRequest request,
            HttpServletResponse response,
            @NonNull FilterChain chain,
            Authentication authResult) throws IOException, ServletException {

        log.info("Authentication successful for user: {}", authResult.getName());
        UserPrincipal user = ((UserPrincipal) Objects.requireNonNull(authResult.getPrincipal()));
        String userId = user.getUsername();
        String email = user.getEmail();

        log.info("Generating JWT token for user: {}, email: {}", userId, email);
        String token = authService.generateToken(userId, user.getAuthorities(), email);

        response.addHeader(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token);
        response.addHeader("X-USER-ID", userId);
        response.addHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization, X-USER-ID");

        log.info("JWT token generated for user: {}", userId);


        String msg = environment.getProperty(
                "security.messages.login_success",
                "Login success"
        );

        response.setContentType("application/json");
        response.getWriter().write("""
            {"message":"%s"}
        """.formatted(escapeJson(msg)));
    }

    @Override
    protected void unsuccessfulAuthentication(@NonNull HttpServletRequest request,
                                              @NonNull HttpServletResponse response,
                                              @NonNull AuthenticationException failed)
            throws IOException {

        SecurityContextHolder.getContextHolderStrategy().clearContext();
        super.getRememberMeServices().loginFail(request, response);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        String defaultMsg = (failed instanceof BadCredentialsException)
                ? environment.getProperty("security.messages.bad_credentials", "Bad credentials")
                : environment.getProperty("security.messages.auth_failed", "Authentication failed");

        response.getWriter().write("""
            {"message":"%s"}
        """.formatted(escapeJson(defaultMsg)));
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
