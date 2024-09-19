package com.esc.lickerz.lickerz_sep.controller;

import com.esc.lickerz.lickerz_sep.util.JwtUtil;
import com.esc.lickerz.lickerz_sep.dto.AuthResDto;
import com.esc.lickerz.lickerz_sep.dto.LoginReqDto;
import com.esc.lickerz.lickerz_sep.entity.UserEntity;
import com.esc.lickerz.lickerz_sep.service.RedisTokenService;

import lombok.extern.slf4j.Slf4j;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/api/dummy/req/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTokenService redisTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReqDto loginReqDto, HttpServletResponse response) {
        UserEntity user = userService.getUserByUsername(loginReqDto.getUsername());
        if (user != null && BCrypt.checkpw(loginReqDto.getPassword(), user.getPasswordHash())) {
            String accessToken = jwtUtil.createToken(user.getUsername(), user.getRole(), user.getId());
            String refreshToken = jwtUtil.createRefreshToken(user.getUsername(), user.getRole(), user.getId());

            // Redis에 리프레시 토큰 저장
            redisTokenService.saveToken(user.getUsername(), refreshToken, jwtUtil.getExpirationTime(refreshToken));

            // 리프레시 토큰을 HTTP-only 쿠키로 설정
            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setMaxAge((int) (jwtUtil.getExpirationTime(refreshToken) / 1000)); // 초 단위로 변환
            response.addCookie(refreshTokenCookie);

            log.info("유저 {} 로그인 성공", user.getUsername());
            
            return ResponseEntity.ok(new AuthResDto(accessToken, null));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenReqDto refreshTokenReqDto) {
        String refreshToken = refreshTokenReqDto.getRefreshToken();
        String username = jwtUtil.getUsername(refreshToken);
        
        log.info("리프레시 토큰 요청 유저: {}", username);
        
        if (redisTokenService.validateToken(username, refreshToken)) {
            UserEntity user = userService.getUserByUsername(username);
            String newAccessToken = jwtUtil.createToken(user.getUsername(), user.getRole(), user.getId());
            log.info("새로운 액세스 토큰 발행됨: {}", username);
            return ResponseEntity.ok(new AuthResDto(newAccessToken, null));
        } else {
            log.warn("잘못된 리프레시 토큰: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 리프레시 토큰");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                String username = jwtUtil.getUsername(token);
                log.info("사용자 {} 로그아웃 시도", username);
                boolean deleted = redisTokenService.deleteToken(username);
                if (deleted) {
                    log.info("사용자 {} 로그아웃 성공", username);
                    Cookie cookie = new Cookie("refreshToken", null);
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    cookie.setHttpOnly(true);
                    cookie.setSecure(true);
                    response.addCookie(cookie);
                    return ResponseEntity.ok("로그아웃 성공");
                } else {
                    log.warn("사용자 {} 토큰 삭제 실패", username);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("토큰 삭제 실패");
                }
            } catch (Exception e) {
                log.error("로그아웃 처리 중 오류 발생", e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 만료되었습니다.");
            }
        }
        return ResponseEntity.badRequest().body("유효하지 않은 토큰");
    }
}
