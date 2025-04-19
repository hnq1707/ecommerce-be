package com.hnq.e_commerce.controllers;

import com.hnq.e_commerce.dto.response.ProductSearchResponseDTO;
import com.hnq.e_commerce.entities.elasticsearch.ProductDocument;
import com.hnq.e_commerce.services.ProductSearchService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products/search")
@RequiredArgsConstructor
@Slf4j
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    // Đồng bộ dữ liệu từ MySQL sang Elasticsearch
    @PostMapping("/sync")
    public ResponseEntity<String> syncProducts() {
        productSearchService.syncProductsToElasticsearch();
        return ResponseEntity.ok("Đồng bộ dữ liệu thành công");
    }

    // Tìm kiếm sản phẩm theo keyword
    @GetMapping
    public ResponseEntity<List<ProductSearchResponseDTO>> searchProducts(
            @RequestParam String keyword) {
        List<ProductDocument> products = productSearchService.searchProducts(keyword);
        List<ProductSearchResponseDTO> response = mapToResponseDTO(products);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/advanced")
    public ResponseEntity<List<ProductSearchResponseDTO>> advancedSearch(
            @RequestParam String keyword) {
        try {
            List<ProductDocument> products = productSearchService.searchWithHighlights(keyword);
            List<ProductSearchResponseDTO> response = mapToResponseDTO(products);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            // Log lỗi để debug
            log.error("Lỗi khi tìm kiếm nâng cao: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of()); // Trả về list rỗng thay vì null
        }
    }




    // Phương thức hỗ trợ chuyển đổi ProductDocument sang DTO
    private List<ProductSearchResponseDTO> mapToResponseDTO(List<ProductDocument> products) {
        return products.stream()
                .map(product -> ProductSearchResponseDTO.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .brand(product.getBrand())
                        .rating(product.getRating())
                        .thumbnail(product.getThumbnail())
                        .isNewArrival(product.isNewArrival())
                        .slug(product.getSlug())
                        .createdAt(product.getCreatedAt())
                        .updatedAt(product.getUpdatedAt())
                        .categoryName(product.getCategoryName())
                        .categoryTypeName(product.getCategoryTypeName())
                        .build())
                .collect(Collectors.toList());
    }
}