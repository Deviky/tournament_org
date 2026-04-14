package com.deviky.Auth_Service.components;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class EmailConfirmationTokenStore {

    private final RedisTemplate<String, String> redisTemplate;

    // Сохраняем токен с TTL
    public void storeToken(String token, String userEmail) {
        redisTemplate.opsForValue().set(token, userEmail, Duration.ofHours(3));
    }

    // Получаем email по токену
    public String getEmail(String token) {
        return redisTemplate.opsForValue().get(token);
    }

    // Удаляем токен после использования
    public void removeToken(String token) {
        redisTemplate.delete(token);
    }
}
