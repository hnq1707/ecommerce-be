package com.hnq.e_commerce.dto;

import com.hnq.e_commerce.entities.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemDetail {

    private String id;
    private Product product;
    private String productVariantId;
    private Integer quantity;
    private Double itemPrice;
}