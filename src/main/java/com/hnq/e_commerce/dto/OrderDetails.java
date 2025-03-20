package com.hnq.e_commerce.dto;

import com.hnq.e_commerce.entities.Address;
import com.hnq.e_commerce.entities.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetails {

    private String id;
    private Date orderDate;
    private Address address;
    private Double totalAmount;
    private Double totalPrice;
    private OrderStatus orderStatus;
    private String paymentMethod;
    private String shipmentNumber;
    private Date expectedDeliveryDate;
    private List<OrderItemDetail> orderItemList;

}