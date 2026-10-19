
package com.linguaflow.linguaflow.controller;

import com.linguaflow.linguaflow.dto.TouristSiteDTO;
import com.linguaflow.linguaflow.service.TouristSiteService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tourist-sites")
public class TouristSiteController {

    private final TouristSiteService touristSiteService;

    public TouristSiteController(
            TouristSiteService touristSiteService) {
        this.touristSiteService = touristSiteService;
    }

    @GetMapping
    public List<TouristSiteDTO> getAllTouristSites() {
        return touristSiteService.getAllTouristSites();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TouristSiteDTO> getTouristSiteById(
            @PathVariable Integer id) {

        return touristSiteService.getTouristSiteById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
