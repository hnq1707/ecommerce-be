package com.hnq.e_commerce.mapper;

import com.hnq.e_commerce.entities.Product;
import com.hnq.e_commerce.entities.elasticsearch.ProductDocument;
import org.springframework.stereotype.Component;

@Component
public class ProductSearchMapper {

    public ProductDocument toDocument(Product product) {
        return ProductDocument.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .brand(product.getBrand())
                .rating(product.getRating())
                .thumbnail(product.getThumbnail())
                .isNewArrival(product.isNewArrival())
                .slug(product.getSlug())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .categoryTypeName(product.getCategoryType() != null ? product.getCategoryType().getName() : null)
                .build();
    }
}