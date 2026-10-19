package com.linguaflow.linguaflow.service;

import com.linguaflow.linguaflow.dto.TouristSiteDetailResponse;
import com.linguaflow.linguaflow.entity.QRCode;
import com.linguaflow.linguaflow.entity.TouristSite;
import com.linguaflow.linguaflow.repository.QRCodeRepository;
import com.linguaflow.linguaflow.repository.TouristSiteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TouristSiteService {

    private final QRCodeRepository qrCodeRepository;
    private final TouristSiteRepository touristSiteRepository;
    private final TouristSiteMapper touristSiteMapper;

    @Transactional(readOnly = true)
    @Cacheable(value = "qr_sites", key = "#qrCodeValue")
    public TouristSiteDetailResponse getSiteDetailByQrCode(String qrCodeValue) {
        QRCode qrCode = qrCodeRepository.findByQrCodeValue(qrCodeValue)
                .filter(code -> Boolean.TRUE.equals(code.getActive()))
                .orElseThrow(() -> new EntityNotFoundException("Mã QR không hợp lệ"));

        Integer siteId = qrCode.getTouristSite().getSiteId();
        TouristSite touristSite = touristSiteRepository.findByIdWithTranslations(siteId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Tourist site not found with id: " + siteId));

        return touristSiteMapper.toDetailResponse(touristSite);
    }
}