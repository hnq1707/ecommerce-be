package com.hnq.e_commerce.controllers;

import com.hnq.e_commerce.dto.ApiResponse;
import com.hnq.e_commerce.dto.ProductDto;
import com.hnq.e_commerce.entities.Product;
import com.hnq.e_commerce.services.ProductService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {

    ProductService productService;


    @GetMapping
    public ApiResponse<Page<ProductDto>> getAllProducts(@RequestParam(required = false, name =
                                                                "categoryId", value = "categoryId") String categoryId, @RequestParam(required = false,
                                                                name = "typeId",
                                                                    value = "typeId") String typeId,
                                                        @RequestParam(required = false) String slug, Pageable pageable, HttpServletResponse response)
    {
        if (StringUtils.isNotBlank(slug)) {
            ProductDto productDto = productService.getProductBySlug(slug);
            return ApiResponse.<Page<ProductDto>>builder()
                    .result(new PageImpl<>(List.of(productDto), pageable, 1))
                    .build();
        } else {
            Page<ProductDto> productPage = productService.getAllProducts(categoryId, typeId, pageable);
            response.setHeader("Content-Range", String.valueOf(productPage.getTotalElements()));
            return ApiResponse.<Page<ProductDto>>builder().result(productPage).build();
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDto> getProductById(@PathVariable String id) {
        ProductDto productDto = productService.getProductById(id);
        return ApiResponse.<ProductDto>builder().result(productDto).build();
    }

    //   create Product
    @PostMapping
    public ApiResponse<Product> createProduct(@RequestBody ProductDto productDto) {
        Product product = productService.addProduct(productDto);
        return ApiResponse.<Product>builder()
                .code(HttpStatus.CREATED.value())
                .message(HttpStatus.CREATED.getReasonPhrase())
                .result(product)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<Product> updateProduct(@RequestBody ProductDto productDto,
                                              @PathVariable String id) {
        Product product = productService.updateProduct(productDto, id);
        return ApiResponse.<Product>builder()
                .message("Update successfully")
                .result(product)
                .build();
    }
}
