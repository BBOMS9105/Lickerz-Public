package com.esc.lickerz.lickerz_sep.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.esc.lickerz.lickerz_sep.util.JwtUtil;
import com.esc.lickerz.lickerz_sep.dto.ChangePasswordReqDto;
import com.esc.lickerz.lickerz_sep.dto.ReviewDto;
import com.esc.lickerz.lickerz_sep.dto.UserDto;
import com.esc.lickerz.lickerz_sep.entity.UserEntity;

import java.util.Map;

import jakarta.validation.Valid;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/dummy/req/auth/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 회원 가입
     * @param userDto 사용자 정보 DTO
     * @return 생성된 사용자
     */
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {

        UserEntity user = userService.createUser(userDto.getUsername(), userDto.getEmail(), userDto.getPassword(), userDto.getRole());
        UserDto responseDto = new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getCreatedAt());

        log.info("사용자 생성됨: {}", user.getUsername());

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    /**
     * 사용자 조회
     * @param id 사용자 ID
     * @return 사용자 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {

        UserEntity user = userService.getUserById(id);
        UserDto responseDto = new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getCreatedAt());

        return ResponseEntity.ok(responseDto);
    }

    /**
     * 사용자 이메일로 조회
     * @param email 사용자 이메일
     * @return 사용자 정보
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {

        UserEntity user = userService.getUserByEmail(email);
        UserDto responseDto = new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getCreatedAt());
        
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 사용자 비밀번호 검증
     * @param email 사용자 이메일
     * @param password 사용자 비밀번호
     * @return 비밀번호 검증 결과
     */
    @PostMapping("/validate")
    public ResponseEntity<String> validateUserPassword(@RequestParam String email, @RequestParam String password) {

        boolean isValid = userService.validateUserPassword(email, password);
        if (isValid) {
            return ResponseEntity.ok("비밀번호 검증 성공");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호 검증 실패");
        }
    }

    /**
     * 사용자 삭제
     * @param id 사용자 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.getUserId(token);

        try {
            userService.deleteUser(userId);

            log.info("사용자 삭제됨: {}", userId);
            return ResponseEntity.ok("사용자 삭제 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자 삭제 실패");
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getUserProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.getUserId(token);
        UserEntity user = userService.getUserById(userId);
        UserDto responseDto = new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getCreatedAt());

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/verify-password")
    public ResponseEntity<String> verifyPassword(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, String> request) {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.getUserId(token);
        String password = request.get("password");
        
        try {
            userService.verifyPassword(userId, password);
            return ResponseEntity.ok("비밀번호가 확인되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestHeader("Authorization") String authHeader, @RequestBody ChangePasswordReqDto request) {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.getUserId(token);
        
        try {
            userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());

            log.info("사용자 비밀번호 변경됨: {}", userId);

            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewDto>> getUserReviews(
        @RequestHeader("Authorization") String authHeader,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.getUserId(token);
        
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortObj = Sort.by(direction, sortParams[0]);
        
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<ReviewDto> reviews = reviewService.getReviewsByUserId(userId, pageable);
        return ResponseEntity.ok(reviews);
    }

}
