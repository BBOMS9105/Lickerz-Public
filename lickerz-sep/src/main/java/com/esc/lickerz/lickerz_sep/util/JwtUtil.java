package com.esc.lickerz.lickerz_sep.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long validityInMilliseconds;

    private Key key;

    private static final long ALLOWED_CLOCK_SKEW_SECONDS = 180;

    /**
     * @Note
     * 토큰 생성 시 사용되는 비밀 키를 초기화
     */
    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * @Note
     * 토큰 생성
     */
    public String createToken(String username, String role, UUID userId) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("role", role);
        claims.put("userId", userId.toString());

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public String getRole(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().get("role", String.class);
    }

    public UUID getUserId(String token) {
        String userIdString = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().get("userId", String.class);
        return UUID.fromString(userIdString); // 문자열을 UUID로 변환
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(ALLOWED_CLOCK_SKEW_SECONDS)
                .build()
                .parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 리프레시 토큰 생성 및 검증
    @Value("${jwt.refresh.expiration}")
    private long refreshValidityInMilliseconds;

//    public String createAccessToken(String username, String role, UUID userId) {
//        return createToken(username, role, userId, validityInMilliseconds);
//    }

    public String createRefreshToken(String username, String role, UUID userId) {
        return createToken(username, role, userId, refreshValidityInMilliseconds);
    }

    private String createToken(String username, String role, UUID userId, long validityInMilliseconds) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("role", role);
        claims.put("userId", userId.toString());
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpirationTime(String token) {
        Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        Date expiration = claims.getBody().getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    /**
     * @Note
     * 비밀번호 재설정 토큰 생성
     */
    public String createPasswordResetToken(String username, UUID userId) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("userId", userId.toString());
        claims.put("purpose", "password_reset");
    
        Date now = new Date();
        Date validity = new Date(now.getTime() + 3600000); // 1시간 유효
    
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refreshToken")) {
                return cookie.getValue();
            }
        }
    }
    return null;
}
}
