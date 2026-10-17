
package tourism_api.service;
import java.util.List;

import org.springframework.stereotype.Service;

import tourism_api.entity.TouristSite;
import tourism_api.repository.TouristSiteRepository;

@Service
public class TouristSiteService {

    private final TouristSiteRepository touristSiteRepository;

    public TouristSiteService(TouristSiteRepository touristSiteRepository) {
        this.touristSiteRepository = touristSiteRepository;
    }

    public List<TouristSite> getAllTouristSites() {
        return touristSiteRepository.findAll();
    }
    public java.util.Optional<TouristSite> getTouristSiteById(Integer siteId) {
    return touristSiteRepository.findById(siteId);
}
public TouristSite createTouristSite(TouristSite touristSite) {
    if (touristSite.getCity() == null
            || touristSite.getCity().isBlank()) {
        touristSite.setCity("TP. Hồ Chí Minh");
    }

    if (touristSite.getIsActive() == null) {
        touristSite.setIsActive(true);
    }

    if (touristSite.getCreatedAt() == null) {
        touristSite.setCreatedAt(java.time.LocalDateTime.now());
    }

    return touristSiteRepository.save(touristSite);
}

public java.util.Optional<TouristSite> updateTouristSite(
        Integer siteId, TouristSite updatedSite) {

    return touristSiteRepository.findById(siteId)
            .map(existingSite -> {
                existingSite.setName(updatedSite.getName());
                existingSite.setCategoryId(updatedSite.getCategoryId());
                existingSite.setDescription(updatedSite.getDescription());
                existingSite.setAddress(updatedSite.getAddress());
                existingSite.setCity(updatedSite.getCity());
                existingSite.setImageUrl(updatedSite.getImageUrl());
                existingSite.setReferenceTicketPrice(
                        updatedSite.getReferenceTicketPrice());

                if (updatedSite.getIsActive() != null) {
                    existingSite.setIsActive(updatedSite.getIsActive());
                }

                return touristSiteRepository.save(existingSite);
            });
}
public boolean deleteTouristSite(Integer siteId) {
    if (!touristSiteRepository.existsById(siteId)) {
        return false;
    }

    touristSiteRepository.deleteById(siteId);
    return true;
}
}
