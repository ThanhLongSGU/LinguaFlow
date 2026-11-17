package com.linguaflow.linguaflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record CreateTouristSiteRequest(
                @NotBlank @Size(max = 200) String name,
                @NotNull Integer categoryId,
                String description,
                String address,
                String city,
                String imageUrl,
                BigDecimal referenceTicketPrice,
                Boolean isActive,
                @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
                @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
                @Min(5) @Max(2000) Integer triggerRadius) {
}