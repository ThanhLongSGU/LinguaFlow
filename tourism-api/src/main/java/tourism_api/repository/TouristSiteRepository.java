
package tourism_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tourism_api.entity.TouristSite;

public interface TouristSiteRepository extends JpaRepository<TouristSite, Integer> {
}
