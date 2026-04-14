package com.deviky.gateway.components;

import com.deviky.Gateway.components.JwtUtil;
import com.deviky.Gateway.models.Role;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtGatewayFilter implements GlobalFilter {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // Public paths — без проверки JWT
        if (path.contains("/public/")) {
            return chain.filter(exchange);
        }

        // Authorization header
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtUtil.parse(token);
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            // Проверка доступа
            if (!checkAccess(path, role)) {
                return forbidden(exchange);
            }

            // Добавляем X-User-Id только для всех, кроме public/private
            if (!path.contains("/public/") && !path.contains("/private/")) {
                ServerHttpRequest request = exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .build();
                return chain.filter(exchange.mutate().request(request).build());
            }

            return chain.filter(exchange);

        } catch (Exception e) {
            return unauthorized(exchange);
        }
    }

    private boolean checkAccess(String path, String role) {

        // Admin имеет доступ ко всем private
        if (Role.ADMIN.name().equals(role)) {
            return true;
        }

        if (path.contains("/player/"))
            return role.equals(Role.PLAYER.name());

        if (path.contains("/organizer/"))
            return role.equals(Role.ORGANIZER.name());

        if (path.contains("/moderator/"))
            return role.equals(Role.MODERATOR.name());

        // Private доступ только для Admin, остальным запрещаем
        if (path.contains("/private/"))
            return false;

        return true;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }
}