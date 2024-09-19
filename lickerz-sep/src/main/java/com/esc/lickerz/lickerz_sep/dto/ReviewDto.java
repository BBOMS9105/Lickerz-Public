package com.esc.lickerz.lickerz_sep.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import com.esc.lickerz.lickerz_sep.dto.InstrumentDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewDto {
    private UUID id;
    private UUID userId;
    private String username;
    private UUID instrumentId;
    private double rating;
    private String comment;
    private int likes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean likedByCurrentUser;
    private InstrumentDto instrumentDto;
}
