package com.hnq.e_commerce.mapper;

import com.hnq.e_commerce.dto.ProductDto;
import com.hnq.e_commerce.dto.ProductResourceDto;
import com.hnq.e_commerce.dto.ProductVariantDto;
import com.hnq.e_commerce.entities.Product;
import com.hnq.e_commerce.entities.ProductVariant;
import com.hnq.e_commerce.entities.Resources;
import com.nimbusds.jose.util.Resource;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import java.util.List;
import java.util.UUID;
@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "categoryType.id", target = "categoryTypeId")
    @Mapping(source = "categoryType.name", target = "categoryTypeName")
    @Mapping(source = "resources", target = "resources") // Đổi productResources thành resources
    @Mapping(source = "productVariants", target = "productVariants") // Đổi variants thành productVariants
    ProductDto toDto(Product product);

    List<ProductDto> toDtoList(List<Product> products);

    // Chuyển từ ProductDto -> Product
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(source = "categoryName", target = "category.name")
    @Mapping(source = "categoryTypeId", target = "categoryType.id")
    @Mapping(source = "categoryTypeName", target = "categoryType.name")
    @Mapping(source = "resources", target = "resources") // Đảm bảo ánh xạ đúng
    @Mapping(source = "productVariants", target = "productVariants")
    Product toEntity(ProductDto productDto);

    List<Product> toProductList(List<ProductDto> productDtos);

    // Chuyển từ ProductVariant -> ProductVariantDto
    @Mapping(source = "product.id", target = "productId")
    ProductVariantDto toVariantDto(ProductVariant variant);

    List<ProductVariantDto> toVariantDtoList(List<ProductVariant> variants);

    // Chuyển từ ProductVariantDto -> ProductVariant
    @Mapping(source = "productId", target = "product.id")
    ProductVariant toEntity(ProductVariantDto variantDto);

    List<ProductVariant> toProductVariantList(List<ProductVariantDto> variantDtos);

    // Kiểm tra lại kiểu dữ liệu của resource nếu cần
    ProductResourceDto toResourceDto(Resource resource);

    List<ProductResourceDto> toResourceDtoList(List<Resources> resources);

    Resources toEntity(ProductResourceDto resourceDto);

    List<Resources> toResourceList(List<ProductResourceDto> resourceDtos);
}
