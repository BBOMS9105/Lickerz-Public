package com.esc.lickerz.lickerz_sep.controller;

import com.esc.lickerz.lickerz_sep.entity.InstrumentEntity;
import com.esc.lickerz.lickerz_sep.service.InstrumentService;
import com.esc.lickerz.lickerz_sep.util.AuthUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
// import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/api/dummy/req/auth/instruments")
public class InstrumentController {

    @Autowired
    private InstrumentService instrumentService;

    @Autowired
    private S3Service s3Service;

    //악기 추가
    @PostMapping("/add")
    public ResponseEntity<?> addInstrument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("model") String model,
            @RequestParam("brand") String brand,
            @RequestParam("type") String type,
            HttpServletRequest request
    ) {
        ResponseEntity<?> forbiddenResponse = AuthUtil.forbiddenIfNotAdmin(request);
        if (forbiddenResponse != null) {
            return forbiddenResponse;
        }

        try {
            String url = s3Service.uploadFile(file);

            InstrumentEntity instrument = new InstrumentEntity();
            instrument.setImgUrl(url);
            instrument.setModel(model);
            instrument.setBrand(brand);
            instrument.setType(type);

            // DB에 저장
            InstrumentEntity savedInstrument = instrumentService.addInstrument(instrument);
            log.info("악기 추가됨 : " + savedInstrument.getModel());

            return ResponseEntity.ok(savedInstrument);

        } catch (IOException e) {
            return ResponseEntity.status(500).body("파일 업로드 중 오류가 발생했습니다.");
        }
    }

    //악기 삭제
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteInstrument(@PathVariable UUID id, HttpServletRequest request) {
        ResponseEntity<?> forbiddenResponse = AuthUtil.forbiddenIfNotAdmin(request);
        if (forbiddenResponse != null) {
            return forbiddenResponse;
        }

        try {
            InstrumentEntity instrument = instrumentService.getInstrumentById(id);
            if (instrument == null) {
                return ResponseEntity.notFound().build();
            }

            instrumentService.deleteInstrument(instrument);
            log.info("악기 삭제됨 : " + instrument.getModel());

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("악기 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("악기 삭제 중 오류가 발생했습니다.");
        }
    }
    
    // 악기 수정
    @PutMapping("/update")
    public ResponseEntity<?> updateInstrument(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("id") UUID id,
            @RequestParam("model") String model,
            @RequestParam("brand") String brand,
            @RequestParam("type") String type,
            HttpServletRequest request
    ) {
        ResponseEntity<?> forbiddenResponse = AuthUtil.forbiddenIfNotAdmin(request);
        if (forbiddenResponse != null) {
            return forbiddenResponse;
        }

        try {
            InstrumentEntity instrument = instrumentService.getInstrumentById(id);
            if (instrument == null) {
                return ResponseEntity.notFound().build();
            }

            instrument.setModel(model);
            instrument.setBrand(brand);
            instrument.setType(type);

            if (file != null && !file.isEmpty()) {
                String url = s3Service.uploadFile(file);
                instrument.setImgUrl(url);
            }

            InstrumentEntity updatedInstrument = instrumentService.updateInstrument(instrument);
            log.info("악기 수정됨 : " + updatedInstrument.getModel());

            return ResponseEntity.ok(updatedInstrument);
        } catch (Exception e) {
            log.error("악기 수정 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("악기 수정 중 오류가 발생했습니다.");
        }
    }
}
