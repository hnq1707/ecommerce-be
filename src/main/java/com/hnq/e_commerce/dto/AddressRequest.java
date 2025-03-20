package com.hnq.e_commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequest {

    private String name;
    private String street;
    private String city;
    private String district;
    private String zipCode;
    private String phoneNumber;
}