package com.esc.lickerz.lickerz_sep.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.esc.lickerz.lickerz_sep.entity.InstrumentEntity;
import com.esc.lickerz.lickerz_sep.service.InstrumentService;

@RestController
@RequestMapping("/api/dummy/pb/instruments")
public class PublicInstrumentController {

    @Autowired
    private InstrumentService instrumentService;

    //모든 악기 조회
    @GetMapping
    public List<InstrumentEntity> getAllInstruments() {
        return instrumentService.getAllInstruments();
    }

    //악기 ID로 조회
    @GetMapping("/{id}")
    public InstrumentEntity getInstrumentById(@PathVariable UUID id) {
        return instrumentService.getInstrumentById(id);
    }

    //악기 이름으로 조회
    @GetMapping("/model/{model}")
    public List<InstrumentEntity> getInstrumentByName(@PathVariable String model) {
        return instrumentService.getInstrumentByName(model);
    }

    //악기 타입으로 조회
    @GetMapping("/type/{type}")
    public List<InstrumentEntity> getInstrumentByType(@PathVariable String type) {
        return instrumentService.getInstrumentByType(type);
    }

    //악기 브랜드로 조회
    @GetMapping("/brand/{brand}")
    public List<InstrumentEntity> getInstrumentByBrand(@PathVariable String brand) {
        return instrumentService.getInstrumentByBrand(brand);
    }

    //악기 타입과 브랜드로 조회
    @GetMapping("/list/{type}/{brand}")
    public ResponseEntity<List<InstrumentEntity>> getInstruments(
        @PathVariable String type,
        @PathVariable String brand
    ) {
        List<InstrumentEntity> instruments = instrumentService.getInstrumentsByTypeAndBrand(type, brand);
        return ResponseEntity.ok(instruments);
    }
}
