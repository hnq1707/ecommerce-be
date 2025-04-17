package com.hnq.e_commerce.repositories;

import com.hnq.e_commerce.entities.elasticsearch.ProductDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {
    
    List<ProductDocument> findByNameContainingOrDescriptionContaining(String name, String description);
    
    List<ProductDocument> findByBrand(String brand);
    
    List<ProductDocument> findByCategoryName(String categoryName);
    
    List<ProductDocument> findByCategoryTypeName(String categoryTypeName);
    
    @Query("{\"bool\": {\"should\": [{\"match\": {\"name\": \"?0\"}}, {\"match\": {\"description\": \"?0\"}}]}}")
    List<ProductDocument> searchByNameOrDescription(String text);
    
    List<ProductDocument> findTop5ByOrderByRatingDesc();
}