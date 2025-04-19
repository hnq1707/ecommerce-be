package com.hnq.e_commerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingStatisticsResponse {
    private String productId;
    private float averageRating;
    private long totalReviews;
    private RatingDistribution ratingDistribution;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingDistribution {
        private long fiveStarCount;
        private long fourStarCount;
        private long threeStarCount;
        private long twoStarCount;
        private long oneStarCount;
        
        // Tính phần trăm cho mỗi rating
        private double fiveStarPercent;
        private double fourStarPercent;
        private double threeStarPercent;
        private double twoStarPercent;
        private double oneStarPercent;
    }
}