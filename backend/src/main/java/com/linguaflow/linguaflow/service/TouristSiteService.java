package com.linguaflow.linguaflow.service;

import com.linguaflow.linguaflow.dto.CreateTouristSiteRequest;
import com.linguaflow.linguaflow.dto.TouristSiteDetailResponse;
import com.linguaflow.linguaflow.dto.TouristSiteDTO;
import com.linguaflow.linguaflow.entity.Category;
import com.linguaflow.linguaflow.entity.QRCode;
import com.linguaflow.linguaflow.entity.TouristSite;
import com.linguaflow.linguaflow.entity.User;
import com.linguaflow.linguaflow.repository.CategoryRepository;
import com.linguaflow.linguaflow.repository.QRCodeRepository;
import com.linguaflow.linguaflow.repository.NarrationRepository;
import com.linguaflow.linguaflow.repository.TouristSiteRepository;
import com.linguaflow.linguaflow.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TouristSiteService {

    private final QRCodeRepository qrCodeRepository;
    private final TouristSiteRepository touristSiteRepository;
    private final TouristSiteMapper touristSiteMapper;
    private final CategoryRepository categoryRepository;
    private final NarrationRepository narrationRepository;
    private final UserRepository userRepository;

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

    @Transactional
    @CacheEvict(value = "qr_sites", allEntries = true)
    public TouristSiteDTO createTouristSite(CreateTouristSiteRequest request) {
        validateTicketPrice(request);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Category not found with id: " + request.categoryId()));
        User createdBy = userRepository.findById(1)
                .orElseThrow(() -> new EntityNotFoundException("Default user not found"));

        TouristSite touristSite = TouristSite.builder()
                .name(request.name())
                .category(category)
                .description(request.description())
                .address(request.address())
                .city(request.city())
                .imageUrl(request.imageUrl())
                .referenceTicketPrice(request.referenceTicketPrice())
                .status(request.isActive())
                .createdBy(createdBy)
                .build();

        return touristSiteMapper.toDto(touristSiteRepository.save(touristSite));
    }

    @Transactional
    @CacheEvict(value = "qr_sites", allEntries = true)
    public TouristSiteDTO updateTouristSite(Integer id, CreateTouristSiteRequest request) {
        validateTicketPrice(request);

        TouristSite touristSite = touristSiteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Tourist site not found with id: " + id));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Category not found with id: " + request.categoryId()));

        touristSite.setName(request.name());
        touristSite.setCategory(category);
        touristSite.setDescription(request.description());
        touristSite.setAddress(request.address());
        touristSite.setCity(request.city());
        touristSite.setImageUrl(request.imageUrl());
        touristSite.setReferenceTicketPrice(request.referenceTicketPrice());
        touristSite.setStatus(request.isActive());

        return touristSiteMapper.toDto(touristSiteRepository.save(touristSite));
    }

    @Transactional
    @CacheEvict(value = "qr_sites", allEntries = true)
    public void deleteTouristSite(Integer id) {
        TouristSite touristSite = touristSiteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Tourist site not found with id: " + id));
        boolean hasQrCodes = qrCodeRepository.existsByTouristSite_SiteId(id);
        boolean hasNarrations = narrationRepository.existsByTouristSite_SiteId(id);

        if (hasQrCodes || !hasNarrations) {
            throw new IllegalStateException("Cannot delete site with active dependencies");
        }

        touristSiteRepository.delete(touristSite);
    }

    private void validateTicketPrice(CreateTouristSiteRequest request) {
        if (request.referenceTicketPrice() != null
                && request.referenceTicketPrice().signum() < 0) {
            throw new IllegalArgumentException("Reference ticket price cannot be negative");
        }
    }
}