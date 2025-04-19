package com.hnq.e_commerce.repositories;

import com.hnq.e_commerce.entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String>, JpaSpecificationExecutor<Review> {
    Page<Review> findByProductId(String productId, Pageable pageable);
    
    Page<Review> findByUserId(String userId, Pageable pageable);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Optional<Float> calculateAverageRatingForProduct(String productId);
    
    boolean existsByUserIdAndProductId(String userId, String productId);
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.rating = :rating")
    long countByProductIdAndRating(String productId, int rating);

    long countByProductId(String productId);

}