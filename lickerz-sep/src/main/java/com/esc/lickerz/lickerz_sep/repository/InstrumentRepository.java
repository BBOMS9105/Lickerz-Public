package com.esc.lickerz.lickerz_sep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.esc.lickerz.lickerz_sep.entity.InstrumentEntity;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;

public interface InstrumentRepository extends JpaRepository<InstrumentEntity, UUID> {

    //모델명으로 조회
    List<InstrumentEntity> findByModel(String model);

    //타입으로 조회
    List<InstrumentEntity> findByType(String type);

    //브랜드명으로 조회
    List<InstrumentEntity> findByBrand(String brand);

    //타입과 브랜드로 조회
    List<InstrumentEntity> findByTypeAndBrand(String type, String brand);

    List<InstrumentEntity> findTop5ByOrderByAverageRatingDescReviewCountDesc(PageRequest of);

    List<InstrumentEntity> findTop5ByOrderByReviewCountDesc(PageRequest of);

}
