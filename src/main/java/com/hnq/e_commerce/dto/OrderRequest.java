package com.hnq.e_commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    private String userId;
    private Date orderDate;
    private String addressId;
    private List<OrderItemRequest> orderItemRequests;
    private Double totalAmount;
    private Double totalPrice;
    private Double discount;
    private String paymentMethod;
    private Date expectedDeliveryDate;
}