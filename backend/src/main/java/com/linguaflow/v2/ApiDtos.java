package com.linguaflow.v2;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public final class ApiDtos {
    private ApiDtos() {}

    public record Site(int siteId, int categoryId, String name, String address, String description,
                       String imageUrl, double latitude, double longitude,
                       int radiusMeters, boolean active) {}
    public record StartPoint(int startPointId, String name, String address, String code, boolean active) {}
    public record Narration(String language, String title, String content, String audioUrl) {}
    public record SiteDetail(Site site, List<Narration> narrations) {}
    public record StartJourneyRequest(@NotBlank String code, boolean demo) {}
    public record StartJourneyResponse(UUID sessionId, String startPointName, String startedAt) {}
    public record HeartbeatRequest(Integer siteId) {}
    public record HeartbeatResponse(Integer currentSiteId, boolean changed) {}
    public record SiteStats(int siteId, String name, double latitude, double longitude,
                            long activeSessions, long totalVisits) {}
    public record AdminStats(long totalStarted, long activeSessions, long totalVisits,
                             long demoActiveSessions, List<SiteStats> sites) {}
    public record SiteEditRequest(
            @NotBlank String name,
            @NotNull Integer categoryId,
            String address,
            String description,
            String imageUrl,
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
            @NotNull @Min(30) @Max(2000) Integer radiusMeters,
            boolean active) {}
    public record StartPointEditRequest(@NotBlank String name, String address) {}
    public record NarrationEditRequest(@NotBlank String title, @NotBlank String vi,
                                       String en, String ja) {}
}
