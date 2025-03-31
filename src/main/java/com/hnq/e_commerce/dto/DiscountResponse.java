package com.hnq.e_commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountResponse {
    private boolean valid;
    private String message;
    private BigDecimal discountAmount;
}

