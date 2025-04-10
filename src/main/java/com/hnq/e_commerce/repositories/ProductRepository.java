package com.hnq.e_commerce.repositories;

import com.hnq.e_commerce.entities.Category;
import com.hnq.e_commerce.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, String>,
        JpaSpecificationExecutor<Product> {
    Product findBySlug(String slug);

    void deleteByCategory(Category category);
}
