
package tourism_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tourism_api.entity.QrCode;
import tourism_api.service.QrCodeService;

@RestController
@RequestMapping("/api/qr-codes")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    public QrCodeController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @GetMapping
    public List<QrCode> getAllQrCodes() {
        return qrCodeService.getAllQrCodes();
    }

    @PostMapping
    public ResponseEntity<QrCode> createQrCode(
            @RequestBody QrCode qrCode) {

        QrCode createdQrCode = qrCodeService.createQrCode(qrCode);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdQrCode);
    }
    
@GetMapping(value = "/{id}/image", produces = MediaType.IMAGE_PNG_VALUE)
public ResponseEntity<byte[]> getQrCodeImage(
        @PathVariable Integer id) {

    byte[] image = qrCodeService.generateQrCodePng(id);

    return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(image);
}

@GetMapping("/{id}")
public ResponseEntity<QrCode> getQrCodeById(
        @PathVariable Integer id) {

    return ResponseEntity.ok(qrCodeService.getQrCodeById(id));
}

@PutMapping("/{id}/status")
public ResponseEntity<QrCode> updateQrCodeStatus(
        @PathVariable Integer id,
        @RequestParam Boolean isActive) {

    return ResponseEntity.ok(
            qrCodeService.updateQrCodeStatus(id, isActive));
}
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteQrCode(
        @PathVariable Integer id) {

    if (qrCodeService.deleteQrCode(id)) {
        return ResponseEntity.noContent().build();
    }

    return ResponseEntity.notFound().build();
}
}
