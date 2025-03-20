package com.hnq.e_commerce.services;

import com.hnq.e_commerce.auth.exceptions.ErrorCode;
import com.hnq.e_commerce.dto.ProductDto;
import com.hnq.e_commerce.entities.Product;
import com.hnq.e_commerce.entities.ProductVariant;
import com.hnq.e_commerce.entities.Resources;
import com.hnq.e_commerce.exception.ResourceNotFoundEx;
import com.hnq.e_commerce.mapper.ProductMapper;
import com.hnq.e_commerce.repositories.ProductRepository;
import com.hnq.e_commerce.specification.ProductSpecification;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    ProductMapper productMapper;


    @Override
    @Transactional
    public Product addProduct(ProductDto productDto) {
        // Kiểm tra sản phẩm đã tồn tại chưa
        Product pd = productRepository.findBySlug(productDto.getSlug());
        if (pd != null) {
            throw new ResourceNotFoundEx(ErrorCode.PRODUCT_EXISTED);
        }

        Product product = productMapper.toEntity(productDto);

        final Product product1 = productRepository.save(product);

        if (productDto.getProductVariants() != null) {
            List<ProductVariant> variants = productMapper.toProductVariantList(productDto.getProductVariants())
                    .stream().peek(variant -> variant.setProduct(product1)).collect(Collectors.toList());
            product.setProductVariants(variants);
        }

        if (productDto.getResources() != null) {
            List<Resources> resources = productMapper.toResourceList(productDto.getResources())
                    .stream().peek(resource -> resource.setProduct(product1)).collect(Collectors.toList());
            product.setResources(resources);
        }

        return productRepository.save(product1);
    }


    @Override
    public Page<ProductDto> getAllProducts(String categoryId, String typeId, Pageable pageable) {
        Specification<Product> productSpecification = Specification.where(null);

        if (categoryId != null) {
            productSpecification = productSpecification.and(ProductSpecification.hasCategoryId(categoryId));
        }
        if (typeId != null) {
            productSpecification = productSpecification.and(ProductSpecification.hasCategoryTypeId(typeId));
        }

        Pageable customPageable = PageRequest.of(pageable.getPageNumber(), 8, pageable.getSort());
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
    public ProductDto getProductById(String id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.PRODUCT_NOT_FOUND));
        return productMapper.toDto(product);
    }

    @Override
    public Product updateProduct(ProductDto productDto, String id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.PRODUCT_NOT_FOUND));
        productDto.setId(product.getId());
        return productRepository.save(productMapper.toEntity(productDto));
    }

    @Override
    public Product fetchProductById(String id) throws Exception {
        return productRepository.findById(id).orElseThrow(BadRequestException::new);
    }

}
