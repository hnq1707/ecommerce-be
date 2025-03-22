package com.hnq.e_commerce.controllers;


import com.hnq.e_commerce.auth.dto.response.OrderResponse;
import com.hnq.e_commerce.dto.ApiResponse;
import com.hnq.e_commerce.dto.OrderDetails;
import com.hnq.e_commerce.dto.OrderRequest;
import com.hnq.e_commerce.entities.OrderStatus;
import com.hnq.e_commerce.services.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@CrossOrigin
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {


    OrderService orderService;


    @PostMapping
    public ApiResponse<?> createOrder(@RequestBody OrderRequest orderRequest, Principal principal) throws Exception {
        OrderResponse orderResponse = orderService.createOrder(orderRequest);
        //return new ResponseEntity<>(order, HttpStatus.CREATED);

        return ApiResponse.builder()
                .code(HttpStatus.CREATED.value())
                .message(HttpStatus.CREATED.getReasonPhrase())
                .result(orderResponse)
                .build();
    }

    @PostMapping("/update-payment")
    public ApiResponse<?> updatePaymentStatus(@RequestBody Map<String, String> request) {
        Map<String, String> response = orderService.updateStatus(request.get("paymentIntent"), request.get("status"));
        return ApiResponse.builder().build();
    }

    @PostMapping("/cancel/{id}")
    public ApiResponse<?> cancelOrder(@PathVariable String id, Principal principal) {
        orderService.cancelOrder(id, principal);
        return ApiResponse.builder().build();
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDetails> getOrder(@PathVariable String id) {
        OrderDetails orderDetails = orderService.getOrderDetails(id);
        return ApiResponse.<OrderDetails>builder().result(orderDetails).build();
    }

    @GetMapping("/users")
    public ApiResponse<Page<OrderDetails>> getOrdersByUser(
            Principal principal,
            @PageableDefault(size = 8, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<Page<OrderDetails>>builder().result(orderService.getOrdersByUser(principal,
                                                                              pageable)).build();
    }

    @GetMapping
    public ApiResponse<Page<OrderDetails>> getAllOrders(@PageableDefault(size = 8, direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<Page<OrderDetails>>builder().result(orderService.getAllOrders(pageable)).build();
    }

    @PostMapping("/update-status/{id}")
    public ApiResponse<?> updateOrderStatus(@PathVariable String id, @RequestBody OrderStatus status) {
        return ApiResponse.builder().result(orderService.updateOrderStatus(id, status)).build();
    }
}