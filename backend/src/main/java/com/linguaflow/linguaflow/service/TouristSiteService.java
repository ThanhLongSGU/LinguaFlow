
package com.linguaflow.linguaflow.service;

import com.linguaflow.linguaflow.dto.TouristSiteDTO;
import com.linguaflow.linguaflow.entity.TouristSite;
import com.linguaflow.linguaflow.repository.TouristSiteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TouristSiteService {

    private final TouristSiteRepository touristSiteRepository;

    public TouristSiteService(
            TouristSiteRepository touristSiteRepository) {
        this.touristSiteRepository = touristSiteRepository;
    }

    @Transactional(readOnly = true)
    public List<TouristSiteDTO> getAllTouristSites() {
        return touristSiteRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<TouristSiteDTO> getTouristSiteById(Integer id) {
        return touristSiteRepository.findById(id)
                .map(this::toDTO);
    }


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
            .referenceTicketPrice(site.getReferenceTicketPrice())
            .isActive(site.getStatus())
            .build();
}

}
