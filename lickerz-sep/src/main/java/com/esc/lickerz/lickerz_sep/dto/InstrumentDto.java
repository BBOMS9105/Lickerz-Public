package com.esc.lickerz.lickerz_sep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InstrumentDto {
    private UUID id;
    private String model;
    private String type;
    private String brand;
    private String imageUrl;
    private double averageRating;
    private int reviewCount;
}
