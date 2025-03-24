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
import org.springframework.data.domain.*;
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
        if (productRepository.findBySlug(productDto.getSlug()) != null) {
            throw new ResourceNotFoundEx(ErrorCode.PRODUCT_EXISTED);
        }

        // Nếu id rỗng, tạo UUID mới cho productDto
        if (productDto.getId() == null || productDto.getId().isBlank()) {
            productDto.setId(UUID.randomUUID().toString());
        }

        // Chuyển đổi DTO sang entity và lưu ban đầu để tạo ID
        Product product = productMapper.toEntity(productDto);
        Product savedProduct = productRepository.save(product);

        // Nếu có productVariants, thiết lập quan hệ với product đã lưu
        if (productDto.getProductVariants() != null) {
            List<ProductVariant> variants = productMapper.toProductVariantList(productDto.getProductVariants())
                    .stream()
                    .peek(variant -> variant.setProduct(savedProduct))
                    .collect(Collectors.toList());
            savedProduct.setProductVariants(variants);
        }

        // Nếu có resources, thiết lập quan hệ với product đã lưu
        if (productDto.getResources() != null) {
            List<Resources> resources = productMapper.toResourceList(productDto.getResources())
                    .stream()
                    .peek(resource -> resource.setProduct(savedProduct))
                    .collect(Collectors.toList());
            savedProduct.setResources(resources);
        }

        // Lưu lại product với variants và resources đã được cập nhật
        return productRepository.save(savedProduct);
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

        Pageable customPageable;
        if (pageable.isUnpaged()) {
            customPageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.unsorted()); // Lấy toàn bộ sản phẩm
        } else {
            customPageable = PageRequest.of(pageable.getPageNumber(), 8, pageable.getSort());
        }
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

    @Transactional
    @Override
    public Product updateProduct(ProductDto productDto, String id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.PRODUCT_NOT_FOUND));

        return productRepository.save(productMapper.toEntity(productDto));
    }

    @Override
    public Product fetchProductById(String id) throws Exception {
        return productRepository.findById(id).orElseThrow(BadRequestException::new);
    }

    @Transactional
    @Override
    public void deleteProduct(String id) {
        // Tìm kiếm sản phẩm theo id, nếu không tìm thấy thì ném ngoại lệ
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.PRODUCT_NOT_FOUND));

        // Xóa sản phẩm (các thực thể liên quan sẽ bị xóa theo cascade)
        productRepository.delete(product);

    }

}
