package com.fooddelivery.filter;

import com.fooddelivery.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Component
public class GatewayJwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/auth",
            "/api/restaurants/search"
    );

    private final JwtUtil jwtUtil;

    public GatewayJwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);
        Optional<Claims> claimsOptional = jwtUtil.parseClaims(token);
        if (claimsOptional.isEmpty()) {
            return unauthorized(exchange);
        }

        Claims claims = claimsOptional.get();
        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        ServerHttpRequest requestWithUserHeaders = exchange.getRequest()
                .mutate()
                .header("X-User-Id", username == null ? "" : username)
                .header("X-User-Role", role == null ? "" : role)
                .build();

        return chain.filter(exchange.mutate().request(requestWithUserHeaders).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
