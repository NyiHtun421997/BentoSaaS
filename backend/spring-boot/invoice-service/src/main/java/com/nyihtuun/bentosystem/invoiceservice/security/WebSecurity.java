package com.nyihtuun.bentosystem.invoiceservice.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement(order = 0)
@EnableMethodSecurity(prePostEnabled = true)
@Configuration
@EnableWebSecurity
public class WebSecurity {

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String ROLE = "role";
    public static final String AUTHORITY = "authority";

    private final AuthService authService;

    @Autowired
    public WebSecurity(AuthService authService) {
        this.authService = authService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthorizationFilter authorizationFilter = new AuthorizationFilter(authService);

        return http.csrf(AbstractHttpConfigurer::disable)
                   .authorizeHttpRequests(auth ->
                                                  auth.requestMatchers("/v3/api-docs")
                                                      .permitAll().anyRequest()
                                                      .authenticated())
                   .addFilterBefore(authorizationFilter, UsernamePasswordAuthenticationFilter.class)
                   .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                   .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                   .exceptionHandling(ex -> ex
                           .authenticationEntryPoint((req, res, e) -> {
                               res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                               res.setContentType("application/json");
                               res.getWriter().write("{\"message\":\"Unauthorized\"}");
                           })
                           .accessDeniedHandler((req, res, e) -> {
                               res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                               res.setContentType("application/json");
                               res.getWriter().write("{\"message\":\"Forbidden\"}");
                           })
                   )
                   .build();
    }
}
