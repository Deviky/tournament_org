package com.deviky.Auth_Service.services;

import com.deviky.Auth_Service.components.TokenBlacklist;
import com.deviky.Auth_Service.models.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final String SECRET = "super-super-secret-key-super-super-secret-key";
    private final TokenBlacklist blacklist;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateAccessToken(Long userId, String username, Role role) {

        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role.name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(5 * 60)))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {

        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(7 * 24 * 3600)))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {

        return Jwts.parser()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {

        final String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return parseToken(token)
                .getExpiration()
                .before(new Date());
    }

    public long getExpirySeconds(String token) {
        try {
            Date exp = parseToken(token).getExpiration();

            long seconds = (exp.getTime() - System.currentTimeMillis()) / 1000;

            return Math.max(seconds, 0);

        } catch (ExpiredJwtException e) {
            // токен уже истёк
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
