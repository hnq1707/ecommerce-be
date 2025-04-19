package com.hnq.e_commerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResponseDTO {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String brand;
    private Float rating;
    private String thumbnail;
    private boolean isNewArrival;
    private String slug;
    private Date createdAt;
    private Date updatedAt;
    private String categoryName;
    private String categoryTypeName;
}