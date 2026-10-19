
package com.linguaflow.linguaflow.controller;

import com.linguaflow.linguaflow.dto.QRCodeDTO;
import com.linguaflow.linguaflow.dto.QRScanResultDTO;
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

    // Request body for creating a QR Code
    public record CreateQRCodeRequest(
            Integer siteId,
            String qrCodeValue
    ) {}

    // 1. GET - All QR Codes
    @GetMapping
    public List<QRCodeDTO> getAllQrCodes() {

        return qrCodeService.getAllQrCodes();
    }

    // 2. GET - QR Code by ID
    @GetMapping("/{id}")
    public QRCodeDTO getQrCodeById(
            @PathVariable Integer id) {

        return qrCodeService.getQrCodeById(id);
    }

    // 3. POST - Create QR Code
    @PostMapping
    public ResponseEntity<QRCodeDTO> createQrCode(
            @RequestBody CreateQRCodeRequest request) {

        QRCodeDTO result = qrCodeService.createQrCode(
                request.siteId(),
                request.qrCodeValue()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }

    // 4. GET - Generate QR Code PNG
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

    // 5. PUT - Enable or disable QR Code
    @PutMapping("/{id}/status")
    public QRCodeDTO updateQrCodeStatus(
            @PathVariable Integer id,
            @RequestParam Boolean isActive) {

        return qrCodeService.updateQrCodeStatus(
                id,
                isActive
        );
    }

    // 6. DELETE - Delete QR Code
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQrCode(
            @PathVariable Integer id) {

        qrCodeService.deleteQrCode(id);

        return ResponseEntity.noContent().build();
    }

    // 7. GET - Resolve scanned QR Code
    @GetMapping("/{id}/resolve")
    public ResponseEntity<QRScanResultDTO> resolveQrCode(
            @PathVariable Integer id) {

        QRScanResultDTO result =
                qrCodeService.resolveQrCode(id);

        return ResponseEntity.ok(result);
    }

    // 8. POST - Record QR Code scan history
    @PostMapping("/{id}/scan")
    public ResponseEntity<QRScanResultDTO> scanQrCode(
            @PathVariable Integer id) {

        QRScanResultDTO result =
                qrCodeService.scanQrCode(id);

        return ResponseEntity.ok(result);
    }
}
