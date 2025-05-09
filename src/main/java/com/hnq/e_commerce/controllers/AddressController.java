package com.hnq.e_commerce.controllers;


import com.hnq.e_commerce.dto.request.AddressRequest;
import com.hnq.e_commerce.dto.response.ApiResponse;
import com.hnq.e_commerce.entities.Address;
import com.hnq.e_commerce.services.AddressService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddressController {


    AddressService addressService;

    @PostMapping
    public ApiResponse<Address> createAddress(@RequestBody AddressRequest addressRequest) {
        Address address = addressService.createAddress(addressRequest);
        return ApiResponse.<Address>builder()
                .code(HttpStatus.CREATED.value())
                .message(HttpStatus.CREATED.getReasonPhrase())
                .result(address)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteAddress(@PathVariable String id) {
        addressService.deleteAddress(id);
        return ApiResponse.builder().build();
    }
}