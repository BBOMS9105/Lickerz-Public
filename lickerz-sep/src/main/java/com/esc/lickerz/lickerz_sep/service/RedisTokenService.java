package com.esc.lickerz.lickerz_sep.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisTokenService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void saveToken(String username, String token, long expirationTime) {
        try {
            redisTemplate.opsForValue().set(username, token, expirationTime, TimeUnit.MILLISECONDS);
            log.info("Token saved for user: {}", username);
        } catch (Exception e) {
            log.error("Error saving token for user: {}", username, e);
        }
    }

    public String getToken(String username) {
        return redisTemplate.opsForValue().get(username);
    }

    public boolean deleteToken(String username) {
        log.info("RedisTokenService.deleteToken() 호출: {}", username);
        try {
            Boolean result = redisTemplate.delete(username);
            log.info("토큰 삭제 결과: {}", result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("토큰 삭제 중 오류 발생", e);
            return false;
        }
    }

    public boolean validateToken(String username, String token) {
        String storedToken = getToken(username);
        log.info("Validating token for user: {}", username);
        log.info("Stored token: {}, Received token: {}", storedToken, token);
        if (storedToken != null && storedToken.equals(token)) {
            return true;
        } else {
            log.warn("Token validation failed for user: {}", username);
            return false;
        }
    }

    public void saveEmailVerificationCode(String email, String code) {
        redisTemplate.opsForValue().set(email + ":verificationCode", code, 5, TimeUnit.MINUTES);
    }

    public boolean verifyEmailCode(String email, String code) {
        String storedCode = redisTemplate.opsForValue().get(email + ":verificationCode");
        return code.equals(storedCode);
    }
}
