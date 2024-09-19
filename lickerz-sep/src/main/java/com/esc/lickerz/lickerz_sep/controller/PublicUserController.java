package com.esc.lickerz.lickerz_sep.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.esc.lickerz.lickerz_sep.util.JwtUtil;
import com.esc.lickerz.lickerz_sep.entity.UserEntity;
import com.esc.lickerz.lickerz_sep.service.EmailService;
import com.esc.lickerz.lickerz_sep.service.RedisTokenService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/dummy/pb/users")
public class PublicUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RedisTokenService redisTokenService;

    // 이메일로 아이디 찾기
    @GetMapping("/find-id/{email}")
    public ResponseEntity<Map<String, String>> findIdByEmail(@PathVariable String email) {

        UserEntity user = userService.getUserByEmail(email);

        if (user != null) {
            Map<String, String> response = new HashMap<>();
            response.put("username", user.getUsername());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    private String generateRandomCode() {
        // 6자리 랜덤 코드 생성 로직
        return String.format("%06d", new Random().nextInt(1000000));
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        UserEntity user = userService.getUserByEmail(email);
        if (user != null) {
            String code = generateRandomCode(); // 6자리 랜덤 코드 생성 메서드
            redisTokenService.saveEmailVerificationCode(email, code);
            emailService.sendVerificationCode(email, code);
            return ResponseEntity.ok("인증 코드가 이메일로 전송되었습니다.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 이메일로 등록된 사용자를 찾을 수 없습니다.");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        if (redisTokenService.verifyEmailCode(email, code)) {
            return ResponseEntity.ok("인증 성공");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 인증 코드입니다.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        String newPassword = request.get("newPassword");
        
        if (redisTokenService.verifyEmailCode(email, code)) {
            UserEntity user = userService.getUserByEmail(email);
            userService.updatePassword(user.getId(), newPassword);
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 코드가 유효하지 않습니다.");
    }
}
