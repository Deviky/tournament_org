package com.deviky.Gateway.components;

import com.deviky.Gateway.models.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtGatewayFilter implements GlobalFilter {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod requestMethod = exchange.getRequest().getMethod();
        String method = requestMethod != null ? requestMethod.name() : "UNKNOWN";

        log.info("Incoming request: {} {}", method, path);

        // Let CORS preflight requests pass before any JWT checks.
        if (requestMethod == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtUtil.parse(token);

            Object userIdClaim = claims.get("userId");
            String userId = userIdClaim != null ? String.valueOf(userIdClaim) : null;
            String role = claims.get("role", String.class);

            if (requiresUserHeader(path) && userId == null) {
                return forbidden(exchange);
            }

            if (!hasAccess(path, role)) {
                return forbidden(exchange);
            }

            if (requiresUserHeader(path)) {
                ServerHttpRequest request = exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .build();

                return chain.filter(exchange.mutate().request(request).build());
            }

            return chain.filter(exchange);

        } catch (ExpiredJwtException e) {
            log.info("ACCESS TOKEN EXPIRED");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().add("X-Token-Expired", "true");
            return exchange.getResponse().setComplete();

        } catch (JwtException e) {
            log.warn("INVALID TOKEN");
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublic(String path) {
        return path.contains("/public/") || path.contains("/auth/");
    }

    private boolean isPrivate(String path) {
        return path.contains("/private/");
    }

    private boolean isPlayer(String path) {
        return path.contains("/player/");
    }

    private boolean isOrganizer(String path) {
        return path.contains("/organizer/");
    }

    private boolean isModerator(String path) {
        return path.contains("/moderator/");
    }

    private boolean requiresUserHeader(String path) {
        return isPlayer(path) || isOrganizer(path) || isModerator(path);
    }

    private boolean hasAccess(String path, String role) {
        Role userRole;

        try {
            userRole = Role.valueOf(role);
        } catch (Exception e) {
            return false;
        }

        if (userRole == Role.ADMIN) {
            return true;
        }

        if (isPrivate(path)) {
            return false;
        }

        if (isPlayer(path)) {
            return userRole == Role.PLAYER;
        }

        if (isOrganizer(path)) {
            return userRole == Role.ORGANIZER;
        }

        if (isModerator(path)) {
            return userRole == Role.MODERATOR;
        }

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
