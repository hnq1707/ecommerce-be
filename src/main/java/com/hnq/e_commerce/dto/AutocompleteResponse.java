package com.hnq.e_commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutocompleteResponse {
    private String suggestion;
    private Double score;
    private String productId;
}