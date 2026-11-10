package com.linguaflow.linguaflow.controller;

import com.linguaflow.linguaflow.dto.QRCodeDTO;
import com.linguaflow.linguaflow.dto.QRScanResultDTO;
import com.linguaflow.linguaflow.service.QRCodeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/qr-codes")
@RequiredArgsConstructor
public class QRCodeController {

    private final QRCodeService qrCodeService;

    @GetMapping
    public ResponseEntity<List<QRCodeDTO>> getQrCodes() {
        return ResponseEntity.ok(qrCodeService.getAllQrCodes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QRCodeDTO> getQrCode(@PathVariable Integer id) {
        return ResponseEntity.ok(qrCodeService.getQrCodeById(id));
    }

    @PostMapping
    public ResponseEntity<QRCodeDTO> createQrCode(@Valid @RequestBody CreateQRCodeRequest request) {
        QRCodeDTO created = qrCodeService.createQrCode(request.siteId(), request.qrCodeValue());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping(value = "/{id}/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCodeImage(@PathVariable Integer id) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCodeService.generateQrCodePng(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<QRCodeDTO> updateQrCodeStatus(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateQRCodeRequest request) {
        return ResponseEntity.ok(qrCodeService.updateQrCodeStatus(id, request.active()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQrCode(@PathVariable Integer id) {
        qrCodeService.deleteQrCode(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/resolution")
    public ResponseEntity<QRScanResultDTO> resolveQrCode(@PathVariable Integer id) {
        return ResponseEntity.ok(qrCodeService.resolveQrCode(id));
    }

    @PostMapping("/{id}/scans")
    public ResponseEntity<QRScanResultDTO> recordQrScan(@PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(qrCodeService.scanQrCode(id));
    }

    public record CreateQRCodeRequest(
            @NotNull Integer siteId,
            @NotBlank @Size(max = 500) String qrCodeValue) {
    }

    public record UpdateQRCodeRequest(@NotNull Boolean active) {
    }
}