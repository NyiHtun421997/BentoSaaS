package com.nyihtuun.bentosystem.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final WebClient webClient;

    public JwtAuthenticationFilter(@Value("${user-service.url}") String authServiceUrl) {
        super(Config.class);
        this.webClient = WebClient.builder()
                                  .baseUrl(authServiceUrl)
                                  .build();
    }

    @Override
    public @NonNull GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            String token =
                    exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (token == null || !token.startsWith("Bearer ")) {
                return onError(exchange, "Missing JWT token!", HttpStatus.UNAUTHORIZED);
            }

            return webClient.get()
                            .uri("/validate-token")
                            .header(HttpHeaders.AUTHORIZATION, token)
                            .exchangeToMono(clientResponse -> {
                                if (clientResponse.statusCode().is2xxSuccessful()) {
                                    return chain.filter(exchange);
                                }

                                if (clientResponse.statusCode() == HttpStatus.UNAUTHORIZED ||
                                        clientResponse.statusCode() == HttpStatus.FORBIDDEN) {
                                    return onError(exchange, "Invalid JWT token!", HttpStatus.UNAUTHORIZED);
                                }

                                return clientResponse.createException().flatMap(Mono::error);
                            });
        });
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errorMessage, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);

        // include error message
        DataBuffer buffer = response.bufferFactory().wrap(errorMessage.getBytes());
        // message handling is complete
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
    }
}
