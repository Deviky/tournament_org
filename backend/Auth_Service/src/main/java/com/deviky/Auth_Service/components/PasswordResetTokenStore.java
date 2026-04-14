package com.deviky.Auth_Service.components;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenStore {

    private final RedisTemplate<String, String> redisTemplate;

    public void storeToken(String token, String email) {
        redisTemplate.opsForValue().set(token, email, Duration.ofHours(1)); // токен живёт 1 час
    }

    public String getEmail(String token) {
        return redisTemplate.opsForValue().get(token);
    }

    public void removeToken(String token) {
        redisTemplate.delete(token);
    }
}
