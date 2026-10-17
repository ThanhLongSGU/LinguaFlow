
package tourism_api.controller;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tourism_api.entity.TouristSite;
import tourism_api.service.TouristSiteService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/tourist-sites")
public class TouristSiteController {

    private final TouristSiteService touristSiteService;

    public TouristSiteController(TouristSiteService touristSiteService) {
        this.touristSiteService = touristSiteService;
    }

    @GetMapping
    public List<TouristSite> getAllTouristSites() {
        return touristSiteService.getAllTouristSites();
    }
@GetMapping("/{id}")
public ResponseEntity<TouristSite> getTouristSiteById(
        @PathVariable Integer id) {

    return touristSiteService.getTouristSiteById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
}
@PostMapping
public ResponseEntity<TouristSite> createTouristSite(
        @Valid @RequestBody TouristSite touristSite) {

    TouristSite createdSite =
            touristSiteService.createTouristSite(touristSite);

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdSite);
}

@PutMapping("/{id}")
public ResponseEntity<TouristSite> updateTouristSite(
        @PathVariable Integer id,
        @Valid @RequestBody TouristSite touristSite) {

    return touristSiteService.updateTouristSite(id, touristSite)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
}
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteTouristSite(
        @PathVariable Integer id) {

    if (touristSiteService.deleteTouristSite(id)) {
        return ResponseEntity.noContent().build();
    }

    return ResponseEntity.notFound().build();
}
}
