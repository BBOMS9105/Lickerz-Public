package com.esc.lickerz.lickerz_sep.service;

import com.esc.lickerz.lickerz_sep.dto.InstrumentDto;
import com.esc.lickerz.lickerz_sep.entity.InstrumentEntity;
import com.esc.lickerz.lickerz_sep.entity.ReviewEntity;
import com.esc.lickerz.lickerz_sep.repository.InstrumentRepository;
import com.esc.lickerz.lickerz_sep.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InstrumentService {

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    //모든 악기 조회
    public List<InstrumentEntity> getAllInstruments() {
        return instrumentRepository.findAll();
    }

    //악기 ID로 조회
    public InstrumentEntity getInstrumentById(UUID id) {
        return instrumentRepository.findById(id).orElse(null);
    }

    //악기 type으로 조회
    public List<InstrumentEntity> getInstrumentByType(String type) {
        return instrumentRepository.findByType(type);
    }

    //악기 이름으로 조회
    public List<InstrumentEntity> getInstrumentByName(String model) {
        return instrumentRepository.findByModel(model);
    }

    //악기 브랜드로 조회
    public List<InstrumentEntity> getInstrumentByBrand(String brand) {
        return instrumentRepository.findByBrand(brand);
    }

    //악기 추가
    public InstrumentEntity addInstrument(InstrumentEntity instrument) {
        return instrumentRepository.save(instrument);
    }

    //악기 삭제
    public void deleteInstrument(InstrumentEntity instrument) {
        instrumentRepository.delete(instrument);
    }

    //악기 수정
    public InstrumentEntity updateInstrument(InstrumentEntity instrument) {
        return instrumentRepository.save(instrument);
    }

    //악기 타입과 브랜드로 조회
    public List<InstrumentEntity> getInstrumentsByTypeAndBrand(String type, String brand) {
        return instrumentRepository.findByTypeAndBrand(type, brand);
    }

    //최근 리뷰된 악기 조회
    public List<InstrumentEntity> getRecentlyReviewedInstruments(int limit) {
        List<ReviewEntity> recentReviews = reviewRepository.findTop5ByOrderByCreatedAtDesc(PageRequest.of(0, limit));
        return recentReviews.stream()
            .map(ReviewEntity::getInstrument)
            .distinct()
            .limit(limit)
            .collect(Collectors.toList());
    }

    //평점이 높은 악기 조회
    public List<InstrumentEntity> getTopRatedInstruments(int limit) {
        return instrumentRepository.findTop5ByOrderByAverageRatingDescReviewCountDesc(PageRequest.of(0, limit));
    }

    //리뷰가 많은 악기 조회
    public List<InstrumentEntity> getMostReviewedInstruments(int limit) {
        return instrumentRepository.findTop5ByOrderByReviewCountDesc(PageRequest.of(0, limit));
    }

}
