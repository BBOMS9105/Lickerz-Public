package com.esc.lickerz.lickerz_sep.controller;

import com.esc.lickerz.lickerz_sep.dto.ReviewDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dummy/pb/instruments/{instrumentId}/reviews")
public class PublicReviewController {

    @Autowired
    private ReviewService reviewService;

    // 리뷰 조회
    @GetMapping
    public ResponseEntity<Page<ReviewDto>> getReviews(
            @PathVariable UUID instrumentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<ReviewDto> reviews = reviewService.getReviewsByInstrumentId(instrumentId, pageable);
        return ResponseEntity.ok(reviews);
    }
}
