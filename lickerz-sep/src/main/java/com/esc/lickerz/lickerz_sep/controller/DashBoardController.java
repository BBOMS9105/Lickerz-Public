package com.esc.lickerz.lickerz_sep.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.esc.lickerz.lickerz_sep.entity.InstrumentEntity;
import com.esc.lickerz.lickerz_sep.service.InstrumentService;
import java.util.List;

@RestController
@RequestMapping("/api/public/dashboard")
public class DashBoardController {

    @Autowired
    private InstrumentService instrumentService;

    @GetMapping("/instruments/recently-reviewed")
    public ResponseEntity<List<InstrumentEntity>> getRecentlyReviewedInstruments() {
        List<InstrumentEntity> instruments = instrumentService.getRecentlyReviewedInstruments(5);
        return ResponseEntity.ok(instruments);
    }

    @GetMapping("/instruments/top-rated")
    public ResponseEntity<List<InstrumentEntity>> getTopRatedInstruments() {
        List<InstrumentEntity> instruments = instrumentService.getTopRatedInstruments(5);
        return ResponseEntity.ok(instruments);
    }

    @GetMapping("/instruments/most-reviewed")
    public ResponseEntity<List<InstrumentEntity>> getMostReviewedInstruments() {
        List<InstrumentEntity> instruments = instrumentService.getMostReviewedInstruments(5);
        return ResponseEntity.ok(instruments);
    }
}
