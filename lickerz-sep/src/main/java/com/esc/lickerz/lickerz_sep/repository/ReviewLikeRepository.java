package com.esc.lickerz.lickerz_sep.repository;

import com.esc.lickerz.lickerz_sep.entity.ReviewLikeEntity;
import com.esc.lickerz.lickerz_sep.entity.ReviewEntity;
import com.esc.lickerz.lickerz_sep.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLikeEntity, UUID> {

    Optional<ReviewLikeEntity> findByReviewAndUser(ReviewEntity review, UserEntity user);

    boolean existsByReviewAndUser(ReviewEntity review, UserEntity user);

    void deleteByReviewAndUser(ReviewEntity review, UserEntity user);

    long countByReview(ReviewEntity review);
}
