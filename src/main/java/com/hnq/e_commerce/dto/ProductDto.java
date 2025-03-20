package com.hnq.e_commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String brand;
    private boolean isNewArrival;
    private Float rating;
    private String categoryId;
    private String thumbnail;
    private String slug;
    private String categoryName;
    private String categoryTypeId;
    private String categoryTypeName;
    private List<ProductVariantDto> productVariants;
    private List<ProductResourceDto> resources;
}
