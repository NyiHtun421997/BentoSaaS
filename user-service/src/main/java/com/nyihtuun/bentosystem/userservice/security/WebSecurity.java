package com.nyihtuun.bentosystem.userservice.security;

import com.nyihtuun.bentosystem.userservice.service.AuthService;
import com.nyihtuun.bentosystem.userservice.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    private final UserService userService;
    private final AuthService authService;
    private final Environment environment;

    @Autowired
    public WebSecurity(UserService userService, AuthService authService, Environment environment) {
        this.userService = userService;
        this.authService = authService;
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   @Value("${login.path}") String loginPath,
                                                   BCryptPasswordEncoder bCryptPasswordEncoder) throws Exception {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(authenticationManager(bCryptPasswordEncoder),
                                                                             authService,
                                                                             environment);
        authenticationFilter.setFilterProcessesUrl(loginPath);

        AuthorizationFilter authorizationFilter = new AuthorizationFilter(authService);

        return http.csrf(AbstractHttpConfigurer::disable)
                   .authorizeHttpRequests(auth ->
                                                  auth.requestMatchers(HttpMethod.GET, "/v1/validate-token")
                                                      .permitAll()
                                                      .requestMatchers(HttpMethod.POST, "/v1/signup/**")
                                                      .permitAll()
                                                      .requestMatchers(HttpMethod.POST, loginPath)
                                                      .permitAll()
                                                      .anyRequest()
                                                      .authenticated())
                   .addFilterBefore(authorizationFilter, UsernamePasswordAuthenticationFilter.class)
                   .addFilter(authenticationFilter)
                   .authenticationManager(authenticationManager(bCryptPasswordEncoder))
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

    private AuthenticationManager authenticationManager(BCryptPasswordEncoder bCryptPasswordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userService);
        authenticationProvider.setPasswordEncoder(bCryptPasswordEncoder);

        ProviderManager providerManager = new ProviderManager(authenticationProvider);
        providerManager.setEraseCredentialsAfterAuthentication(false);

        return providerManager;
    }
}
