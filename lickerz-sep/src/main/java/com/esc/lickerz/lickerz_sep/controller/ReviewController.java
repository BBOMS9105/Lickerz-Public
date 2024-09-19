package com.esc.lickerz.lickerz_sep.controller;

import com.esc.lickerz.lickerz_sep.dto.ReviewDto;
import com.esc.lickerz.lickerz_sep.entity.ReviewEntity;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/instruments/{instrumentId}/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // 리뷰 작성
    @PostMapping
    public ResponseEntity<?> createReview(@PathVariable UUID instrumentId, @RequestBody ReviewDto reviewDto) {

        log.info("ReviewDto: {}", reviewDto.getUserId());

        if (reviewService.hasUserReviewedInstrument(reviewDto.getUserId(), instrumentId)) {
            return ResponseEntity.badRequest().body("이미 이 악기에 대한 리뷰를 작성하셨습니다.");
        }
        try {
            ReviewEntity savedReview = reviewService.createReview(instrumentId, reviewDto.getUserId(), reviewDto.getRating(), reviewDto.getComment());
            log.info("리뷰 작성됨: {}, {}", reviewDto.getUsername(), reviewDto.getInstrumentId());
            return ResponseEntity.ok(savedReview);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 리뷰 수정
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
        @PathVariable UUID instrumentId,
        @PathVariable UUID reviewId,
        @RequestBody ReviewDto reviewDto,
        @RequestAttribute("userId") UUID userId,
        @RequestAttribute("role") String role) {
        try {
            ReviewEntity updatedReview = reviewService.updateReview(reviewId, reviewDto, userId, role);

            log.info("리뷰 수정됨: {}, {}", reviewDto.getUsername(), reviewDto.getInstrumentId());
            return ResponseEntity.ok(updatedReview);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
        @PathVariable UUID instrumentId,
        @PathVariable UUID reviewId,
        @RequestAttribute("userId") UUID userId,
        @RequestAttribute("role") String role) {
        try {
            reviewService.deleteReview(reviewId, userId, role);

            log.info("리뷰 삭제됨: {}", reviewId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 리뷰 좋아요
    @PostMapping("/{reviewId}/like")
    public ResponseEntity<?> likeReview(
        @PathVariable UUID instrumentId,
        @PathVariable UUID reviewId,
        @RequestAttribute("userId") UUID userId) {
        try {
            ReviewEntity updatedReview = reviewService.likeReview(reviewId, userId);
            ReviewDto reviewDto = reviewService.convertToDto(updatedReview, userId);
            return ResponseEntity.ok(reviewDto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 리뷰 좋아요 취소
    @DeleteMapping("/{reviewId}/like")
    public ResponseEntity<?> unlikeReview(
        @PathVariable UUID instrumentId,
        @PathVariable UUID reviewId,
        @RequestAttribute("userId") UUID userId) {
        try {
            ReviewEntity updatedReview = reviewService.removeLike(reviewId, userId);
            ReviewDto reviewDto = reviewService.convertToDto(updatedReview, userId);
            return ResponseEntity.ok(reviewDto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 리뷰 조회
    @GetMapping
    public ResponseEntity<Page<ReviewDto>> getReviewsForInstrument(
        @PathVariable UUID instrumentId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortObj = Sort.by(direction, sortParams[0]);
        
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<ReviewDto> reviews = reviewService.getReviewsForInstrument(instrumentId, pageable);
        return ResponseEntity.ok(reviews);
    }
}
