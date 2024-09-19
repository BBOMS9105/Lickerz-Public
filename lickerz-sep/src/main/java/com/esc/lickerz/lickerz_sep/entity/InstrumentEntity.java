package com.esc.lickerz.lickerz_sep.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Table(name = "instruments")
public class InstrumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "brand_name", nullable = false)
    private String brand;

    // double precision
    @Column(name = "average_rating", columnDefinition = "NUMERIC(2, 1) DEFAULT 0.0")
    private double averageRating;

    @Column(name = "review_count", columnDefinition = "INTEGER DEFAULT 0")
    private int reviewCount;

    @Column(nullable = false, length = 255)
    private String imgUrl;

    // 기본 생성자
    public InstrumentEntity() {}

    // 생성자
    public InstrumentEntity(String type, String model, String brand) {
        this.type = type;
        this.model = model;
        this.brand = brand;
    }

    public void setAverageRating(double averageRating2) {
        this.averageRating = averageRating2;
    }

}
