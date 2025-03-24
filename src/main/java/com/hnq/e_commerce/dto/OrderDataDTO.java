package com.hnq.e_commerce.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDataDTO {
    private String month;
    private int orders;
}
