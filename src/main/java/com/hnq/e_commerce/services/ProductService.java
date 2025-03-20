package com.hnq.e_commerce.services;

import com.hnq.e_commerce.dto.ProductDto;
import com.hnq.e_commerce.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {
    Product addProduct(ProductDto product);

    Page<ProductDto> getAllProducts(String categoryId, String typeId, Pageable pageable);

    ProductDto getProductBySlug(String slug);

    ProductDto getProductById(String id);

    Product updateProduct(ProductDto productDto, String id);

    Product fetchProductById(String uuid) throws Exception;
}
