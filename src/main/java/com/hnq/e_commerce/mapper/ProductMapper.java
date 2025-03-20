package com.hnq.e_commerce.mapper;

import com.hnq.e_commerce.dto.ProductDto;
import com.hnq.e_commerce.dto.ProductResourceDto;
import com.hnq.e_commerce.dto.ProductVariantDto;
import com.hnq.e_commerce.entities.Product;
import com.hnq.e_commerce.entities.ProductVariant;
import com.hnq.e_commerce.entities.Resources;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "categoryType.id", target = "categoryTypeId")
    @Mapping(source = "categoryType.name", target = "categoryTypeName")
    @Mapping(source = "resources", target = "resources")
    @Mapping(source = "productVariants", target = "productVariants") // Đổi variants thành productVariants
    ProductDto toDto(Product product);

    List<ProductDto> toDtoList(List<Product> products);

    // Chuyển từ ProductDto -> Product
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(source = "categoryName", target = "category.name")
    @Mapping(source = "categoryTypeId", target = "categoryType.id")
    @Mapping(source = "categoryTypeName", target = "categoryType.name")
    @Mapping(target = "productVariants", ignore = true) // Gán riêng trong service
    @Mapping(target = "resources", ignore = true)
    Product toEntity(ProductDto productDto);

    List<Product> toProductList(List<ProductDto> productDtos);

    // Chuyển từ ProductVariant -> ProductVariantDto
    @Mapping(source = "product.id", target = "productId")
    ProductVariantDto toVariantDto(ProductVariant variant);

    List<ProductVariantDto> toVariantDtoList(List<ProductVariant> variants);

    // Chuyển từ ProductVariantDto -> ProductVariant
    @Mapping(source = "productId", target = "product.id")
    @Mapping(target = "product", ignore = true)
    ProductVariant toEntity(ProductVariantDto variantDto);

    List<ProductVariant> toProductVariantList(List<ProductVariantDto> variantDtos);

    // Kiểm tra lại kiểu dữ liệu của resource nếu cần
    ProductResourceDto toResourceDto(Resources resource);

    List<ProductResourceDto> toResourceDtoList(List<Resources> resources);
    @Mapping(target = "product", ignore = true)
    Resources toEntity(ProductResourceDto resourceDto);

    List<Resources> toResourceList(List<ProductResourceDto> resourceDtos);
}
