package com.hnq.e_commerce.specification;

import com.hnq.e_commerce.entities.Product;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ProductSpecification {

    public static Specification<Product> hasCategoryId(String categorId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("category").get("id"), categorId);
    }

    public static Specification<Product> hasCategoryTypeId(String typeId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("categoryType").get("id"), typeId);
    }
}