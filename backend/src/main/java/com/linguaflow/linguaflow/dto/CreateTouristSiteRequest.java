
package com.linguaflow.linguaflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateTouristSiteRequest(

        @NotBlank
        @Size(max = 200)
        String name,

        @NotNull
        Integer categoryId,

        String description,

        String address,

        String city,

        String imageUrl,

        BigDecimal referenceTicketPrice,

        Boolean isActive
) {
}
