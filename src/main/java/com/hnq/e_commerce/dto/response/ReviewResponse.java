package com.hnq.e_commerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private String id;
    private Integer rating;
    private String comment;
    private String productId;
    private String productName;
    private String userId;
    private String username;
    private boolean isGuestReview;
    private Date createdAt;
    private Date updatedAt;
}