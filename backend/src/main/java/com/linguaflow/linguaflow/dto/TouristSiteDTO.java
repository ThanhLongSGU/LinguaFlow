
package com.linguaflow.linguaflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TouristSiteDTO {

    private Integer siteId;
    private String name;

    private Integer categoryId;

    private String description;
    private String address;
    private String city;
    private String imageUrl;

    private BigDecimal referenceTicketPrice;

    private Boolean isActive;
}
