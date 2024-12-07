package com.hnq.e_commerce.services;

import com.hnq.e_commerce.dto.ProductDto;
import com.hnq.e_commerce.entities.Product;

import java.util.List;

public interface ProductService {
    public Product addProduct(ProductDto product);
    public List<Product> getAllProducts();
}
