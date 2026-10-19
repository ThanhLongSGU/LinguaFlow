
package com.linguaflow.linguaflow.controller;

import com.linguaflow.linguaflow.dto.CreateTouristSiteRequest;
import com.linguaflow.linguaflow.dto.TouristSiteDTO;
import com.linguaflow.linguaflow.service.TouristSiteService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tourist-sites")
public class TouristSiteController {

    private final TouristSiteService touristSiteService;

    public TouristSiteController(
            TouristSiteService touristSiteService) {

        this.touristSiteService = touristSiteService;
    }

    // GET all tourist sites
    @GetMapping
    public List<TouristSiteDTO> getAllTouristSites() {

        return touristSiteService.getAllTouristSites();
    }

    // GET tourist site by ID
    @GetMapping("/{id}")
    public ResponseEntity<TouristSiteDTO> getTouristSiteById(
            @PathVariable Integer id) {

        return touristSiteService.getTouristSiteById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // POST create tourist site
    @PostMapping
    public ResponseEntity<TouristSiteDTO> createTouristSite(
            @Valid @RequestBody CreateTouristSiteRequest request) {

        TouristSiteDTO created =
                touristSiteService.createTouristSite(request);

        URI location = URI.create(
                "/api/tourist-sites/" + created.getSiteId()
        );

        return ResponseEntity
                .created(location)
                .body(created);
    }

@PutMapping("/{id}")
public ResponseEntity<TouristSiteDTO> updateTouristSite(
        @PathVariable Integer id,
        @Valid @RequestBody CreateTouristSiteRequest request) {

    TouristSiteDTO updated =
            touristSiteService.updateTouristSite(id, request);

    return ResponseEntity.ok(updated);
}

    // DELETE tourist site
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTouristSite(
            @PathVariable Integer id) {

        touristSiteService.deleteTouristSite(id);

        return ResponseEntity.noContent().build();
    }

}
