package com.nyihtuun.bentosystem.userservice.controller;

import com.nyihtuun.bentosystem.userservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.nyihtuun.bentosystem.userservice.controller.ApiPaths.VERSION1;
import static com.nyihtuun.bentosystem.userservice.security.WebSecurity.BEARER_PREFIX;

@RestController
@RequestMapping(VERSION1)
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthService authService;

    @GetMapping("/validate-token")
    public ResponseEntity<Void> validateToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        if (token == null || !token.startsWith(BEARER_PREFIX)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return authService.validateToken(token.substring(7))
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
