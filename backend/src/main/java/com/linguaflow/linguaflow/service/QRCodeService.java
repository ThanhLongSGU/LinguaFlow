
package com.linguaflow.linguaflow.service;

// ZXing - Generate QR Code images
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

// DTO
import com.linguaflow.linguaflow.dto.QRCodeDTO;
import com.linguaflow.linguaflow.dto.QRScanResultDTO;

// Entity
import com.linguaflow.linguaflow.entity.QRCode;
import com.linguaflow.linguaflow.entity.QRScanLog;
import com.linguaflow.linguaflow.entity.TouristSite;

// Repository
import com.linguaflow.linguaflow.repository.QRCodeRepository;
import com.linguaflow.linguaflow.repository.QRScanLogRepository;
import com.linguaflow.linguaflow.repository.TouristSiteRepository;

// Spring Boot
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// Java
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

    // ==============================
    // 1. REPOSITORIES
    // ==============================

    private final QRCodeRepository qrCodeRepository;
    private final TouristSiteRepository touristSiteRepository;
    private final QRScanLogRepository qrScanLogRepository;

    // Constructor Dependency Injection
    public QRCodeService(
            QRCodeRepository qrCodeRepository,
            TouristSiteRepository touristSiteRepository,
            QRScanLogRepository qrScanLogRepository) {

        this.qrCodeRepository = qrCodeRepository;
        this.touristSiteRepository = touristSiteRepository;
        this.qrScanLogRepository = qrScanLogRepository;
    }

    // ==============================
    // 2. GET ALL QR CODES
    // ==============================

    @Transactional(readOnly = true)
    public List<QRCodeDTO> getAllQrCodes() {

        return qrCodeRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ==============================
    // 3. GET QR CODE BY ID
    // ==============================

    @Transactional(readOnly = true)
    public QRCodeDTO getQrCodeById(Integer id) {

        QRCode qrCode = findQrCode(id);

        return toDTO(qrCode);
    }

    // ==============================
    // 4. CREATE QR CODE
    // ==============================

    @Transactional
    public QRCodeDTO createQrCode(
            Integer siteId,
            String qrCodeValue) {

        // Validate Site ID
        if (siteId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Site ID is required"
            );
        }

        // Find tourist site
        TouristSite site = touristSiteRepository.findById(siteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tourist site not found"
                ));

        // Validate QR Code value
        if (qrCodeValue == null || qrCodeValue.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "QR Code value is required"
            );
        }

        String value = qrCodeValue.trim();

        // Check maximum length
        if (value.length() > 500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "QR Code value exceeds 500 characters"
            );
        }

        // Validate URL
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
                    "QR Code must contain a valid HTTP or HTTPS URL"
            );
        }

        // Check duplicate URL
        if (qrCodeRepository.existsByQrCodeValue(value)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "QR Code value already exists"
            );
        }

        // Create QR Code entity
        QRCode qrCode = new QRCode();

        qrCode.setTouristSite(site);
        qrCode.setQrCodeValue(value);
        qrCode.setActive(true);

        // Save to SQL Server
        QRCode saved = qrCodeRepository.saveAndFlush(qrCode);

        return toDTO(saved);
    }

    // ==============================
    // 5. GENERATE QR CODE PNG
    // ==============================

    @Transactional(readOnly = true)
    public byte[] generateQrCodePng(Integer id) {

        QRCode qrCode = findQrCode(id);

        try {
            Map<EncodeHintType, Object> hints =
                    new EnumMap<>(EncodeHintType.class);

            hints.put(
                    EncodeHintType.CHARACTER_SET,
                    StandardCharsets.UTF_8.name()
            );

            hints.put(EncodeHintType.MARGIN, 2);

            // Generate QR Code matrix
            BitMatrix matrix = new QRCodeWriter().encode(
                    qrCode.getQrCodeValue(),
                    BarcodeFormat.QR_CODE,
                    300,
                    300,
                    hints
            );

            // Convert matrix to PNG
            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(
                    matrix,
                    "PNG",
                    output
            );

            return output.toByteArray();

        } catch (WriterException | IOException e) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to generate QR Code image",
                    e
            );
        }
    }

    // ==============================
    // 6. UPDATE QR CODE STATUS
    // ==============================

    @Transactional
    public QRCodeDTO updateQrCodeStatus(
            Integer id,
            Boolean active) {

        if (active == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "QR Code status is required"
            );
        }

        QRCode qrCode = findQrCode(id);

        qrCode.setActive(active);

        QRCode updated = qrCodeRepository.save(qrCode);

        return toDTO(updated);
    }

    // ==============================
    // 7. DELETE QR CODE
    // ==============================

    @Transactional
    public void deleteQrCode(Integer id) {

        QRCode qrCode = findQrCode(id);

        // Prevent deleting QR Codes with scan history
        if (!qrCode.getScanLogs().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "QR Code has scan history and cannot be deleted"
            );
        }

        qrCodeRepository.delete(qrCode);
    }

    // ==============================
    // 8. RESOLVE QR CODE
    // ==============================

    @Transactional(readOnly = true)
    public QRScanResultDTO resolveQrCode(Integer qrId) {

        // Find QR Code
        QRCode qrCode = findQrCode(qrId);

        // Check QR Code status
        if (!Boolean.TRUE.equals(qrCode.getActive())) {

            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "QR Code is inactive"
            );
        }

        // Get associated tourist site
        TouristSite site = qrCode.getTouristSite();

        if (site == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Tourist site not found"
            );
        }

        // Check tourist site status
        if (!Boolean.TRUE.equals(site.getStatus())) {

            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "Tourist site is inactive"
            );
        }

        // Return tourist site information
        return new QRScanResultDTO(
                qrCode.getQrId(),
                site.getSiteId(),
                site.getName(),
                site.getCity()
        );
    }

    // ==============================
    // 9. RECORD QR CODE SCAN
    // ==============================

    @Transactional
    public QRScanResultDTO scanQrCode(Integer qrId) {

        // Step 1: Validate QR Code and tourist site
        QRScanResultDTO result = resolveQrCode(qrId);

        // Step 2: Find QR Code entity
        QRCode qrCode = findQrCode(qrId);

        // Step 3: Create scan log
        QRScanLog scanLog = new QRScanLog();

        scanLog.setQrCode(qrCode);
        scanLog.setScannedAt(LocalDateTime.now());

        // Step 4: Save scan history to SQL Server
        qrScanLogRepository.saveAndFlush(scanLog);

        // Step 5: Return site information
        return result;
    }

    // ==============================
    // 10. FIND QR CODE
    // ==============================

    private QRCode findQrCode(Integer id) {

        return qrCodeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "QR Code not found"
                ));
    }

    // ==============================
    // 11. CONVERT ENTITY TO DTO
    // ==============================

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
