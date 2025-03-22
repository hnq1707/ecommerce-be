package com.hnq.e_commerce.services;

import com.hnq.e_commerce.dto.ProductDto;
import com.hnq.e_commerce.entities.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    @Transactional
    Product addProduct(ProductDto product);

    Page<ProductDto> getAllProducts(String categoryId, String typeId, Pageable pageable);

    ProductDto getProductBySlug(String slug);

    ProductDto getProductById(String id);

    @Transactional
    Product updateProduct(ProductDto productDto, String id);

    Product fetchProductById(String uuid) throws Exception;

    @Transactional
    void deleteProduct(String id);
}
