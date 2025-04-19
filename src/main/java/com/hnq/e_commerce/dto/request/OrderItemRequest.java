package com.hnq.e_commerce.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {

    private String productId;
    private String productVariantId;
    private Double discount;
    private Double price;
    private Integer quantity;
}