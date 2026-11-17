package com.linguaflow.linguaflow.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.linguaflow.linguaflow.dto.QRCodeDTO;
import com.linguaflow.linguaflow.dto.QRScanResultDTO;
import com.linguaflow.linguaflow.entity.QRCode;
import com.linguaflow.linguaflow.entity.QRScanLog;
import com.linguaflow.linguaflow.entity.TouristSite;
import com.linguaflow.linguaflow.repository.QRCodeRepository;
import com.linguaflow.linguaflow.repository.QRScanLogRepository;
import com.linguaflow.linguaflow.repository.TouristSiteRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class QRCodeService {

    private final QRCodeRepository qrCodeRepository;
    private final TouristSiteRepository touristSiteRepository;
    private final QRScanLogRepository qrScanLogRepository;

    public QRCodeService(
            QRCodeRepository qrCodeRepository,
            TouristSiteRepository touristSiteRepository,
            QRScanLogRepository qrScanLogRepository) {
        this.qrCodeRepository = qrCodeRepository;
        this.touristSiteRepository = touristSiteRepository;
        this.qrScanLogRepository = qrScanLogRepository;
    }

    @Transactional(readOnly = true)
    public List<QRCodeDTO> getAllQrCodes() {
        return qrCodeRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public QRCodeDTO getQrCodeById(Integer id) {
        QRCode qrCode = findQrCode(id);
        return toDTO(qrCode);
    }

    @Transactional
    public QRCodeDTO createQrCode(Integer siteId, String qrCodeValue) {
        if (siteId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Site ID is required");
        }

        TouristSite site = touristSiteRepository.findById(siteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tourist site not found"));

        if (qrCodeValue == null || qrCodeValue.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "QR Code value is required");
        }

        String value = qrCodeValue.trim();
        if (value.length() > 500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "QR Code value exceeds 500 characters");
        }

        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();
            if (scheme == null
                    || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))
                    || uri.getHost() == null) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "QR Code must contain a valid HTTP or HTTPS URL");
        }

        if (qrCodeRepository.existsByQrCodeValue(value)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "QR Code value already exists");
        }

        QRCode qrCode = new QRCode();
        qrCode.setTouristSite(site);
        qrCode.setQrCodeValue(value);
        qrCode.setActive(true);

        return toDTO(qrCodeRepository.saveAndFlush(qrCode));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "qr_images", key = "#id")
    public byte[] generateQrCodePng(Integer id) {
        QRCode qrCode = findQrCode(id);

        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(EncodeHintType.MARGIN, 2);

            BitMatrix matrix = new QRCodeWriter().encode(
                    qrCode.getQrCodeValue(),
                    BarcodeFormat.QR_CODE,
                    300,
                    300,
                    hints);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return output.toByteArray();
        } catch (WriterException | IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to generate QR Code image",
                    exception);
        }
    }

    @Transactional
    @CacheEvict(value = "qr_images", key = "#id")
    public QRCodeDTO updateQrCodeStatus(Integer id, Boolean active) {
        if (active == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "QR Code status is required");
        }

        QRCode qrCode = findQrCode(id);
        qrCode.setActive(active);
        return toDTO(qrCodeRepository.save(qrCode));
    }

    @Transactional
    @CacheEvict(value = "qr_images", key = "#id")
    public void deleteQrCode(Integer id) {
        QRCode qrCode = findQrCode(id);
        if (!qrCode.getScanLogs().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "QR Code has scan history and cannot be deleted");
        }
        qrCodeRepository.delete(qrCode);
    }

    @Transactional(readOnly = true)
    public QRScanResultDTO resolveQrCode(Integer qrId) {
        QRCode qrCode = findQrCode(qrId);
        if (!Boolean.TRUE.equals(qrCode.getActive())) {
            throw new ResponseStatusException(HttpStatus.GONE, "QR Code is inactive");
        }

        TouristSite site = qrCode.getTouristSite();
        if (site == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tourist site not found");
        }
        if (!Boolean.TRUE.equals(site.getStatus())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Tourist site is inactive");
        }

        return new QRScanResultDTO(qrCode.getQrId(), site.getSiteId(), site.getName(), site.getCity());
    }

    @Transactional
    public QRScanResultDTO scanQrCode(Integer qrId) {
        QRScanResultDTO result = resolveQrCode(qrId);
        QRCode qrCode = findQrCode(qrId);

        QRScanLog scanLog = new QRScanLog();
        scanLog.setQrCode(qrCode);
        scanLog.setScannedAt(LocalDateTime.now());
        qrScanLogRepository.saveAndFlush(scanLog);

        return result;
    }

    private QRCode findQrCode(Integer id) {
        return qrCodeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "QR Code not found"));
    }

    private QRCodeDTO toDTO(QRCode qrCode) {
        return QRCodeDTO.builder()
                .qrId(qrCode.getQrId())
                .siteId(qrCode.getTouristSite().getSiteId())
                .qrCodeValue(qrCode.getQrCodeValue())
                .active(qrCode.getActive())
                .createdAt(qrCode.getCreatedAt())
                .build();
    }
}
