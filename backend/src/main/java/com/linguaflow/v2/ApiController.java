package com.linguaflow.v2;

import com.linguaflow.v2.ApiDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final LinguaFlowService service;
    public ApiController(LinguaFlowService service) { this.service = service; }

    @GetMapping("/public/health")
    public Map<String,String> health() { return Map.of("status","ok","project","LinguaFlowV2"); }

    @GetMapping("/public/sites")
    public List<Site> sites() { return service.listSites(false); }

    @GetMapping("/public/sites/{siteId}")
    public SiteDetail site(@PathVariable int siteId) { return service.getSiteDetail(siteId); }

    @GetMapping("/public/start/{code}")
    public StartPoint startPoint(@PathVariable String code) { return service.findStartPointForCode(code); }

    @PostMapping("/public/journeys/start")
    public StartJourneyResponse begin(@Valid @RequestBody StartJourneyRequest req) { return service.startJourney(req); }

    private UUID token(String auth) {
        if (auth==null || !auth.startsWith("Bearer "))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Missing session token");
        try { return UUID.fromString(auth.substring(7).trim()); }
        catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid session token");
        }
    }

    @PostMapping("/public/journeys/heartbeat")
    public HeartbeatResponse heartbeat(@RequestHeader(value="Authorization", required=false) String bearer,
                                       @RequestBody HeartbeatRequest req) {
        return service.heartbeat(token(bearer),req);
    }

    @PostMapping("/public/journeys/end")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void end(@RequestHeader(value="Authorization",required=false) String bearer) {
        service.endJourney(token(bearer));
    }

    @GetMapping("/admin/stats")
    public AdminStats stats() { return service.adminStats(); }

    @GetMapping("/admin/sites")
    public List<Site> adminSites() { return service.listSites(true); }

    @GetMapping("/admin/categories")
    public List<Map<String,Object>> categories() { return service.categories(); }

    @PostMapping("/admin/sites")
    @ResponseStatus(HttpStatus.CREATED)
    public Site addSite(@Valid @RequestBody SiteEditRequest req) { return service.saveSite(null,req); }

    @PutMapping("/admin/sites/{siteId}")
    public Site updateSite(@PathVariable int siteId,@Valid @RequestBody SiteEditRequest req) {
        return service.saveSite(siteId,req);
    }

    @DeleteMapping("/admin/sites/{siteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableSite(@PathVariable int siteId) { service.disableSite(siteId); }

    @GetMapping("/admin/start-points")
    public List<StartPoint> starts() { return service.listStartPoints(); }

    @PostMapping("/admin/start-points")
    @ResponseStatus(HttpStatus.CREATED)
    public StartPoint createStart(@Valid @RequestBody StartPointEditRequest req) {
        return service.createStartPoint(req);
    }

    @PutMapping("/admin/sites/{siteId}/narration")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void narration(@PathVariable int siteId,@Valid @RequestBody NarrationEditRequest req) {
        service.saveNarration(siteId,req);
    }
}
