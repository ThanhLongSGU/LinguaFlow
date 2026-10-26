package com.linguaflow.linguaflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbySiteDTO {
    private Integer siteId;
    private String name;
    private String address;
    private String imageUrl;
    private Double distance; // Khoảng cách tới người dùng (km hoặc mét tùy logic xử lý)
}