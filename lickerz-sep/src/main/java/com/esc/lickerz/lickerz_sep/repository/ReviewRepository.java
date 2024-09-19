package com.esc.lickerz.lickerz_sep.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;

import com.esc.lickerz.lickerz_sep.entity.ReviewEntity;

import java.util.UUID;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM ReviewEntity r WHERE r.instrument.id = :instrumentId")
    double getAverageRatingByInstrumentId(UUID instrumentId);

    int countByInstrumentId(UUID instrumentId);

    Page<ReviewEntity> findByInstrumentId(UUID instrumentId, Pageable pageable);

    boolean existsByUserIdAndInstrumentId(UUID userId, UUID instrumentId);

    List<ReviewEntity> findTop5ByOrderByCreatedAtDesc(Pageable pageable);

    List<ReviewEntity> findByUserId(UUID id);

    Page<ReviewEntity> findByUserId(UUID userId, Pageable pageable);

    }
