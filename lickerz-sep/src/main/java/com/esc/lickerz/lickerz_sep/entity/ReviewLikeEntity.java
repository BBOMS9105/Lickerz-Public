package com.esc.lickerz.lickerz_sep.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "review_likes")
public class ReviewLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private ReviewEntity review;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 기본 생성자
    public ReviewLikeEntity() {}

    // 생성자
    public ReviewLikeEntity(ReviewEntity review, UserEntity user) {
        this.review = review;
        this.user = user;
    }
}
