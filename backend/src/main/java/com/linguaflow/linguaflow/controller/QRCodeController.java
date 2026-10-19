
package com.linguaflow.linguaflow.controller;

import com.linguaflow.linguaflow.dto.QRCodeDTO;
import com.linguaflow.linguaflow.service.QRCodeService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qr-codes")
public class QRCodeController {

    private final QRCodeService qrCodeService;

    public QRCodeController(QRCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    public record CreateQRCodeRequest(
            Integer siteId,
            String qrCodeValue
    ) {}

    @GetMapping
    public List<QRCodeDTO> getAllQrCodes() {
        return qrCodeService.getAllQrCodes();
    }

    @GetMapping("/{id}")
    public QRCodeDTO getQrCodeById(
            @PathVariable Integer id) {

        return qrCodeService.getQrCodeById(id);
    }

    @PostMapping
    public ResponseEntity<QRCodeDTO> createQrCode(
            @RequestBody CreateQRCodeRequest request) {

        QRCodeDTO result = qrCodeService.createQrCode(
                request.siteId(),
                request.qrCodeValue());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }

    @GetMapping(
            value = "/{id}/image",
            produces = MediaType.IMAGE_PNG_VALUE
    )
    public ResponseEntity<byte[]> getQrCodeImage(
            @PathVariable Integer id) {

        byte[] image = qrCodeService.generateQrCodePng(id);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }

    @PutMapping("/{id}/status")
    public QRCodeDTO updateQrCodeStatus(
            @PathVariable Integer id,
            @RequestParam Boolean isActive) {

        return qrCodeService.updateQrCodeStatus(id, isActive);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQrCode(
            @PathVariable Integer id) {

        qrCodeService.deleteQrCode(id);
        return ResponseEntity.noContent().build();
    }
}
