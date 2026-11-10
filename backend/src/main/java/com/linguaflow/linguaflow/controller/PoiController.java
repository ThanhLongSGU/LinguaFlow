package com.linguaflow.linguaflow.controller;

import com.linguaflow.linguaflow.dto.PoiSyncResponse;
import com.linguaflow.linguaflow.service.PoiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
public class PoiController {

    private final PoiService poiService;

    @GetMapping("/poi")
    public ResponseEntity<PoiSyncResponse> syncPoi(
            @RequestParam(defaultValue = "0") long clientVersion) {
        if (clientVersion < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientVersion cannot be negative");
        }

        long serverVersion = poiService.getCurrentDataVersion();
        String versionTag = "\"" + serverVersion + "\"";
        if (clientVersion == serverVersion) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .header(HttpHeaders.ETAG, versionTag)
                    .header("X-Data-Version", Long.toString(serverVersion))
                    .build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.ETAG, versionTag)
                .header("X-Data-Version", Long.toString(serverVersion))
                .body(poiService.getPoiSnapshot(serverVersion));
    }
}