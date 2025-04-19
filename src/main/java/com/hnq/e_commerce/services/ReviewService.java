package com.hnq.e_commerce.services;

import com.hnq.e_commerce.dto.request.ReviewRequest;
import com.hnq.e_commerce.dto.response.RatingStatisticsResponse;
import com.hnq.e_commerce.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    
    ReviewResponse createReview(ReviewRequest reviewRequest, String userId);
    
    Page<ReviewResponse> getProductReviews(String productId, Pageable pageable);
    
    Page<ReviewResponse> getUserReviews(String userId, Pageable pageable);
    
    ReviewResponse getReviewById(String reviewId);
    
    ReviewResponse updateReview(String reviewId, ReviewRequest reviewRequest, String userId);
    
    void deleteReview(String reviewId, String userId);
    
    boolean canUserReviewProduct(String userId, String productId, String orderId);
    
    Float getAverageRatingForProduct(String productId);
    
    void updateProductRating(String productId);
    RatingStatisticsResponse getProductRatingStatistics(String productId);

}
