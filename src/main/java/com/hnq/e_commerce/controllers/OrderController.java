package com.hnq.e_commerce.controllers;


import com.hnq.e_commerce.auth.dto.response.OrderResponse;
import com.hnq.e_commerce.dto.ApiResponse;
import com.hnq.e_commerce.dto.OrderDetails;
import com.hnq.e_commerce.dto.OrderRequest;
import com.hnq.e_commerce.services.OrderService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/order")
@CrossOrigin
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {



    OrderService orderService;



    @PostMapping
    public ApiResponse<?> createOrder(@RequestBody OrderRequest orderRequest, Principal principal) throws Exception {
        OrderResponse orderResponse = orderService.createOrder(orderRequest, principal);
        //return new ResponseEntity<>(order, HttpStatus.CREATED);

        return ApiResponse.builder()
                .code(HttpStatus.CREATED.value())
                .message(HttpStatus.CREATED.getReasonPhrase())
                .build();
    }

    @PostMapping("/update-payment")
    public ApiResponse<?> updatePaymentStatus(@RequestBody Map<String,String> request){
        Map<String,String> response = orderService.updateStatus(request.get("paymentIntent"),request.get("status"));
        return ApiResponse.builder().build();
    }

    @PostMapping("/cancel/{id}")
    public ApiResponse<?> cancelOrder(@PathVariable UUID id,Principal principal){
        orderService.cancelOrder(id,principal);
        return ApiResponse.builder().build();
    }

    @GetMapping("/user")
    public ApiResponse<List<OrderDetails>> getOrderByUser(Principal principal) {
        List<OrderDetails> orders = orderService.getOrdersByUser(principal.getName());
        return ApiResponse.<List<OrderDetails>>builder().result(orders).build();
    }

}