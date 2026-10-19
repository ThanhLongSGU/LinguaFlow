
package com.linguaflow.linguaflow.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import com.linguaflow.linguaflow.dto.QRCodeDTO;
import com.linguaflow.linguaflow.entity.QRCode;
import com.linguaflow.linguaflow.entity.TouristSite;
import com.linguaflow.linguaflow.repository.QRCodeRepository;
import com.linguaflow.linguaflow.repository.TouristSiteRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class QRCodeService {

    private final QRCodeRepository qrCodeRepository;
    private final TouristSiteRepository touristSiteRepository;

    public QRCodeService(
            QRCodeRepository qrCodeRepository,
            TouristSiteRepository touristSiteRepository) {

        this.qrCodeRepository = qrCodeRepository;
        this.touristSiteRepository = touristSiteRepository;
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
        return toDTO(findQrCode(id));
    }

    @Transactional
    public QRCodeDTO createQrCode(
            Integer siteId,
            String qrCodeValue) {

        if (siteId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Site ID is required");
        }

        TouristSite site = touristSiteRepository.findById(siteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tourist site not found"));

        if (qrCodeValue == null || qrCodeValue.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "QR Code value is required");
        }

        String value = qrCodeValue.trim();

        if (value.length() > 500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "QR Code value exceeds 500 characters");
        }

        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();

            if (scheme == null
                    || (!scheme.equalsIgnoreCase("http")
                    && !scheme.equalsIgnoreCase("https"))
                    || uri.getHost() == null) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "QR Code must contain a valid HTTP or HTTPS URL");
        }

        if (qrCodeRepository.existsByQrCodeValue(value)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "QR Code value already exists");
        }

        QRCode qrCode = new QRCode();
        qrCode.setTouristSite(site);
        qrCode.setQrCodeValue(value);
        qrCode.setActive(true);

        QRCode saved = qrCodeRepository.saveAndFlush(qrCode);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public byte[] generateQrCodePng(Integer id) {
        QRCode qrCode = findQrCode(id);

        try {
            Map<EncodeHintType, Object> hints =
                    new EnumMap<>(EncodeHintType.class);

            hints.put(
                    EncodeHintType.CHARACTER_SET,
                    StandardCharsets.UTF_8.name());

            hints.put(EncodeHintType.MARGIN, 2);

            BitMatrix matrix = new QRCodeWriter().encode(
                    qrCode.getQrCodeValue(),
                    BarcodeFormat.QR_CODE,
                    300,
                    300,
                    hints);

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(
                    matrix,
                    "PNG",
                    output);

            return output.toByteArray();

        } catch (WriterException | IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to generate QR Code image",
                    e);
        }
    }

    @Transactional
    public QRCodeDTO updateQrCodeStatus(
            Integer id,
            Boolean active) {

        if (active == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "QR Code status is required");
        }

        QRCode qrCode = findQrCode(id);
        qrCode.setActive(active);

        return toDTO(qrCodeRepository.save(qrCode));
    }

    @Transactional
    public void deleteQrCode(Integer id) {
        QRCode qrCode = findQrCode(id);

        if (!qrCode.getScanLogs().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "QR Code has scan history and cannot be deleted");
        }

        qrCodeRepository.delete(qrCode);
    }

    private QRCode findQrCode(Integer id) {
        return qrCodeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "QR Code not found"));
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
