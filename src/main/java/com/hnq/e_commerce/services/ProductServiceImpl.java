package com.hnq.e_commerce.services;

import com.hnq.e_commerce.auth.exceptions.ErrorCode;
import com.hnq.e_commerce.dto.ProductDto;
import com.hnq.e_commerce.entities.Product;
import com.hnq.e_commerce.exception.ResourceNotFoundEx;
import com.hnq.e_commerce.mapper.ProductMapper;
import com.hnq.e_commerce.repositories.ProductRepository;
import com.hnq.e_commerce.specification.ProductSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    ProductMapper productMapper;


    @Override
    public Product addProduct(ProductDto productDto) {
        Product product = productMapper.toEntity(productDto);
        return productRepository.save(product);
    }

    @Override
    public Page<ProductDto> getAllProducts(UUID categoryId, UUID typeId, Pageable pageable) {
        Specification<Product> productSpecification = Specification.where(null);

        if (categoryId != null) {
            productSpecification = productSpecification.and(ProductSpecification.hasCategoryId(categoryId));
        }
        if (typeId != null) {
            productSpecification = productSpecification.and(ProductSpecification.hasCategoryTypeId(typeId));
        }

        Pageable customPageable = PageRequest.of(pageable.getPageNumber(), 9, pageable.getSort());
        Page<Product> products = productRepository.findAll(productSpecification, customPageable);
        List<ProductDto> productDtos = productMapper.toDtoList(products.getContent());
        return new PageImpl<>(productDtos, products.getPageable(), products.getTotalElements());

    }
    @Override
    public ProductDto getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug);
        if (null == product) {
            throw new ResourceNotFoundEx(ErrorCode.PRODUCT_NOT_FOUND);
        }
        ProductDto productDto = productMapper.toDto(product);
        productDto.setCategoryId(product.getCategory().getId());
        productDto.setCategoryTypeId(product.getCategoryType().getId());
        productDto.setProductVariants(productMapper.toVariantDtoList(product.getProductVariants()));
        productDto.setResources(productMapper.toResourceDtoList(product.getResources()));
        return productDto;
    }

    @Override
    public ProductDto getProductById(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.PRODUCT_NOT_FOUND));
        ProductDto productDto = productMapper.toDto(product);
        productDto.setCategoryId(product.getCategory().getId());
        productDto.setCategoryTypeId(product.getCategoryType().getId());
        productDto.setProductVariants(productMapper.toVariantDtoList(product.getProductVariants()));
        productDto.setResources(productMapper.toResourceDtoList(product.getResources()));
        return productDto;
    }

    @Override
    public Product updateProduct(ProductDto productDto, UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.PRODUCT_NOT_FOUND));
        productDto.setId(product.getId());
        return productRepository.save(productMapper.toEntity(productDto));
    }

    @Override
    public Product fetchProductById(UUID id) throws Exception {
        return productRepository.findById(id).orElseThrow(BadRequestException::new);
    }

}
