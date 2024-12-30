package com.hnq.e_commerce.services;

import com.hnq.e_commerce.dto.ProductDto;
import com.hnq.e_commerce.entities.Product;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    public Product addProduct(ProductDto product);
    public List<ProductDto> getAllProducts(UUID categoryId, UUID typeId);

    ProductDto getProductBySlug(String slug);

    ProductDto getProductById(UUID id);

    Product updateProduct(ProductDto productDto, UUID id);

    Product fetchProductById(UUID uuid) throws Exception;
}
