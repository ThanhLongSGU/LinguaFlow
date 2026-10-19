
package com.linguaflow.linguaflow.service;

import com.linguaflow.linguaflow.dto.CreateTouristSiteRequest;
import com.linguaflow.linguaflow.dto.TouristSiteDTO;

import com.linguaflow.linguaflow.entity.Category;
import com.linguaflow.linguaflow.entity.TouristSite;
import com.linguaflow.linguaflow.entity.User;

import com.linguaflow.linguaflow.repository.CategoryRepository;
import com.linguaflow.linguaflow.repository.TouristSiteRepository;
import com.linguaflow.linguaflow.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class TouristSiteService {

    private final TouristSiteRepository touristSiteRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final Environment environment;

    @Value("${app.dev.allow-seed-creator:false}")
    private boolean allowSeedCreator;

    @Value("${app.dev.seed-creator-username:}")
    private String seedCreatorUsername;

    public TouristSiteService(
            TouristSiteRepository touristSiteRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            Environment environment) {

        this.touristSiteRepository = touristSiteRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.environment = environment;
    }

    // Check local demo mode
    private void checkLocalDemoMode() {

        if (!environment.acceptsProfiles(Profiles.of("local"))
                || !allowSeedCreator) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Demo site management is disabled"
            );
        }
    }

    // GET all tourist sites
    @Transactional(readOnly = true)
    public List<TouristSiteDTO> getAllTouristSites() {

        return touristSiteRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // GET tourist site by ID
    @Transactional(readOnly = true)
    public Optional<TouristSiteDTO> getTouristSiteById(Integer id) {

        return touristSiteRepository.findById(id)
                .map(this::toDTO);
    }

    // POST create tourist site
    @Transactional
    public TouristSiteDTO createTouristSite(
            CreateTouristSiteRequest request) {

        checkLocalDemoMode();

        Category category = categoryRepository
                .findById(request.categoryId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Category does not exist"
                ));

        User creator = userRepository
                .findByUsername(seedCreatorUsername)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Demo creator not found"
                ));

        validateTicketPrice(request.referenceTicketPrice());

        TouristSite site = new TouristSite();

        site.setName(request.name());
        site.setCategory(category);
        site.setCreatedBy(creator);
        site.setDescription(request.description());
        site.setAddress(request.address());
        site.setCity(request.city());
        site.setImageUrl(request.imageUrl());
        site.setReferenceTicketPrice(
                request.referenceTicketPrice()
        );

        site.setStatus(
                request.isActive() != null
                        ? request.isActive()
                        : true
        );

        TouristSite savedSite = touristSiteRepository.save(site);

        return toDTO(savedSite);
    }

    // PUT update tourist site
    @Transactional
    public TouristSiteDTO updateTouristSite(
            Integer id,
            CreateTouristSiteRequest request) {

        checkLocalDemoMode();

        TouristSite site = touristSiteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tourist site not found"
                ));

        Category category = categoryRepository
                .findById(request.categoryId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Category does not exist"
                ));

        validateTicketPrice(request.referenceTicketPrice());

        site.setName(request.name());
        site.setCategory(category);
        site.setDescription(request.description());
        site.setAddress(request.address());
        site.setCity(request.city());
        site.setImageUrl(request.imageUrl());
        site.setReferenceTicketPrice(
                request.referenceTicketPrice()
        );

        site.setStatus(
                request.isActive() != null
                        ? request.isActive()
                        : true
        );

        TouristSite updatedSite = touristSiteRepository.save(site);

        return toDTO(updatedSite);
    }

    // DELETE tourist site
    @Transactional
    public void deleteTouristSite(Integer id) {

        checkLocalDemoMode();

        TouristSite site = touristSiteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tourist site not found"
                ));

        boolean hasQRCodes = site.getQrCodes() != null
                && !site.getQrCodes().isEmpty();

        boolean hasNarrations = site.getNarrations() != null
                && !site.getNarrations().isEmpty();

        if (hasQRCodes || hasNarrations) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete site with QR codes or narrations"
            );
        }

        touristSiteRepository.delete(site);
        touristSiteRepository.flush();
    }

    // Validate ticket price
    private void validateTicketPrice(BigDecimal price) {

        if (price != null
                && price.compareTo(BigDecimal.ZERO) < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ticket price cannot be negative"
            );
        }
    }

    // Convert Entity to DTO
    private TouristSiteDTO toDTO(TouristSite site) {

        return TouristSiteDTO.builder()
                .siteId(site.getSiteId())
                .name(site.getName())
                .categoryId(
                        site.getCategory() != null
                                ? site.getCategory().getCategoryId()
                                : null
                )
                .description(site.getDescription())
                .address(site.getAddress())
                .city(site.getCity())
                .imageUrl(site.getImageUrl())
                .referenceTicketPrice(
                        site.getReferenceTicketPrice()
                )
                .isActive(site.getStatus())
                .build();
    }
}
