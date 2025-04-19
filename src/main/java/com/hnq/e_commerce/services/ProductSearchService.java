package com.hnq.e_commerce.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.hnq.e_commerce.entities.Product;
import com.hnq.e_commerce.entities.elasticsearch.ProductDocument;

import com.hnq.e_commerce.mapper.ProductSearchMapper;
import com.hnq.e_commerce.repositories.ProductRepository;
import com.hnq.e_commerce.repositories.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;
    private final ProductSearchMapper productMapper;
    private final ElasticsearchClient elasticsearchClient;


    // Đồng bộ dữ liệu từ MySQL sang Elasticsearch
    public void syncProductsToElasticsearch() {
        List<Product> products = productRepository.findAll();
        List<ProductDocument> productDocuments = products.stream()
                .map(productMapper::toDocument)
                .collect(Collectors.toList());

        productSearchRepository.saveAll(productDocuments);
    }

    // Tìm kiếm sản phẩm theo từ khóa
    public List<ProductDocument> searchProducts(String keyword) {
        return productSearchRepository.searchByNameOrDescription(keyword);
    }


    // Thêm/cập nhật sản phẩm vào Elasticsearch
    public void indexProduct(Product product) {
        ProductDocument productDocument = productMapper.toDocument(product);
        productSearchRepository.save(productDocument);
    }

    // Xóa sản phẩm khỏi Elasticsearch
    public void removeProduct(String productId) {
        productSearchRepository.deleteById(productId);
    }

    // Tìm kiếm nâng cao với highlighting
    public List<ProductDocument> searchWithHighlights(String keyword) throws IOException {
        SearchRequest searchRequest = SearchRequest.of(sr -> sr
                .index("products")
                .query(q -> q
                        .multiMatch(mm -> mm
                                .fields("name", "description")
                                .query(keyword)
                                .fuzziness("AUTO")
                        )
                )
                .highlight(h -> h
                        .fields("name", hf -> hf
                                .preTags("<em>")
                                .postTags("</em>")
                        )
                        .fields("description", hf -> hf
                                .preTags("<em>")
                                .postTags("</em>")
                        )
                )
        );

        SearchResponse<ProductDocument> response = elasticsearchClient.search(
                searchRequest, ProductDocument.class);

        return response.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
    }


}