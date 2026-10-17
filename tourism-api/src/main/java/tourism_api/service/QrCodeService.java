
package tourism_api.service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import tourism_api.entity.QrCode;
import tourism_api.repository.QrCodeRepository;
import tourism_api.repository.TouristSiteRepository;

@Service
public class QrCodeService {

    private final QrCodeRepository qrCodeRepository;
    private final TouristSiteRepository touristSiteRepository;

    public QrCodeService(
            QrCodeRepository qrCodeRepository,
            TouristSiteRepository touristSiteRepository) {
        this.qrCodeRepository = qrCodeRepository;
        this.touristSiteRepository = touristSiteRepository;
    }

    public List<QrCode> getAllQrCodes() {
        return qrCodeRepository.findAll();
    }

    public QrCode createQrCode(QrCode qrCode) {
        if (qrCode.getSiteId() == null
                || !touristSiteRepository.existsById(qrCode.getSiteId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Địa điểm du lịch không tồn tại");
        }

        if (qrCode.getQrCodeValue() == null
                || qrCode.getQrCodeValue().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Giá trị QR Code không được để trống");
        }

        if (qrCode.getQrCodeValue().length() > 500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Giá trị QR Code không được vượt quá 500 ký tự");
        }

        if (qrCode.getIsActive() == null) {
            qrCode.setIsActive(true);
        }

        if (qrCode.getCreatedAt() == null) {
            qrCode.setCreatedAt(LocalDateTime.now());
        }

        return qrCodeRepository.save(qrCode);
    }
    
public byte[] generateQrCodePng(Integer qrId) {
    QrCode qrCode = qrCodeRepository.findById(qrId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy mã QR"));

    if (qrCode.getQrCodeValue() == null
            || qrCode.getQrCodeValue().isBlank()) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Giá trị QR Code không được để trống");
    }

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

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        MatrixToImageWriter.writeToStream(
                matrix, "PNG", outputStream);

        return outputStream.toByteArray();

    } catch (WriterException | IOException e) {
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Không thể tạo ảnh QR Code",
                e);
    }
}

public QrCode getQrCodeById(Integer qrId) {
    return qrCodeRepository.findById(qrId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy mã QR"));
}

public QrCode updateQrCodeStatus(Integer qrId, Boolean isActive) {
    if (isActive == null) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Trạng thái QR không được để trống");
    }

    QrCode qrCode = qrCodeRepository.findById(qrId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy mã QR"));

    qrCode.setIsActive(isActive);

    return qrCodeRepository.save(qrCode);
}
public boolean deleteQrCode(Integer qrId) {
    if (!qrCodeRepository.existsById(qrId)) {
        return false;
    }

    qrCodeRepository.deleteById(qrId);
    return true;
}
}
