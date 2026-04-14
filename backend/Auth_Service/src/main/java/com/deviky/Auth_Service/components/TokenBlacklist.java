package com.deviky.Auth_Service.components;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class TokenBlacklist {

    private final RedisTemplate<String, String> redisTemplate;

    public void revoke(String token, long secondsUntilExpiry) {
        redisTemplate.opsForValue().set(token, "revoked", Duration.ofSeconds(secondsUntilExpiry));
    }

    public boolean isRevoked(String token) {
        return redisTemplate.hasKey(token);
    }
}
