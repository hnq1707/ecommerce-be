package com.hnq.e_commerce.services;

import com.hnq.e_commerce.auth.dto.response.OrderResponse;
import com.hnq.e_commerce.auth.entities.User;
import com.hnq.e_commerce.auth.exceptions.ErrorCode;
import com.hnq.e_commerce.auth.repositories.UserRepository;
import com.hnq.e_commerce.auth.services.EmailService;
import com.hnq.e_commerce.dto.OrderDetails;
import com.hnq.e_commerce.dto.OrderItemDetail;
import com.hnq.e_commerce.dto.OrderRequest;
import com.hnq.e_commerce.entities.*;
import com.hnq.e_commerce.exception.ResourceNotFoundEx;
import com.hnq.e_commerce.repositories.OrderRepository;
import com.stripe.model.PaymentIntent;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {


    OrderRepository orderRepository;


    ProductService productService;


    PaymentIntentService paymentIntentService;
   UserRepository userRepository;
   EmailService emailService;
//    NotificationService notificationService;


    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) throws Exception {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        User user = userRepository.findByEmail(name).orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.USER_NOT_EXISTED));

        // Kiểm tra và tìm địa chỉ
        Address address = user.getAddressList().stream()
                .filter(address1 -> orderRequest.getAddressId().equals(address1.getId()))
                .findFirst()
                .orElseThrow(BadRequestException::new);

        // Tạo đối tượng Order
        Order order = Order.builder()
                .user(user)
                .address(address)
                .totalAmount(orderRequest.getTotalAmount())
                .totalPrice(orderRequest.getTotalPrice())
                .orderDate(orderRequest.getOrderDate())
                .discount(orderRequest.getDiscount())
                .expectedDeliveryDate(orderRequest.getExpectedDeliveryDate())
                .paymentMethod(orderRequest.getPaymentMethod())
                .orderStatus(OrderStatus.PENDING)
                .build();

        // Tạo danh sách OrderItem
        List<OrderItem> orderItems = orderRequest.getOrderItemRequests().stream()
                .map(orderItemRequest -> {
                    try {
                        Product product = productService.fetchProductById(orderItemRequest.getProductId());
                        return OrderItem.builder()
                                .product(product)
                                .productVariantId(orderItemRequest.getProductVariantId())
                                .quantity(orderItemRequest.getQuantity())
                                .itemPrice(orderItemRequest.getPrice())
                                .order(order)
                                .build();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to fetch product or create order item", e);
                    }
                })
                .toList();

        // Gán danh sách OrderItem vào Order
        order.setOrderItemList(orderItems);

        // Tạo đối tượng Payment
        Payment payment = new Payment();
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(new Date());
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(order.getPaymentMethod());
        order.setPayment(payment);

        // Lưu Order vào cơ sở dữ liệu
        Order savedOrder = orderRepository.save(order);
        emailService.sendOrderConfirmation(user, order);
//        notificationService.sendOrderCreatedNotification(savedOrder);




        // Tạo OrderResponse
        OrderResponse orderResponse = OrderResponse.builder()
                .paymentMethod(orderRequest.getPaymentMethod())
                .orderId(savedOrder.getId())
                .build();

        // Nếu phương thức thanh toán là CARD, tạo PaymentIntent
        if (Objects.equals(orderRequest.getPaymentMethod(), "CARD")) {
            orderResponse.setCredentials(paymentIntentService.createPaymentIntent(order));
        }

        return orderResponse;
    }

    public Map<String, String> updateStatus(String paymentIntentId, String status) {

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            if (paymentIntent != null && paymentIntent.getStatus().equals("succeeded")) {
                String orderId = paymentIntent.getMetadata().get("orderId");
                Order order = orderRepository.findById(orderId).orElseThrow(BadRequestException::new);
                Payment payment = order.getPayment();
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment.setPaymentMethod(paymentIntent.getPaymentMethod());
                order.setPaymentMethod(paymentIntent.getPaymentMethod());
                order.setOrderStatus(OrderStatus.IN_PROGRESS);
                order.setShipmentTrackingNumber(UUID.randomUUID().toString());
                order.setPayment(payment);
                Order savedOrder = orderRepository.save(order);
                Map<String, String> map = new HashMap<>();
                map.put("orderId", String.valueOf(savedOrder.getId()));
//                notificationService.sendOrderStatusUpdateNotification(order, OrderStatus.IN_PROGRESS);

                return map;
            } else {
                throw new IllegalArgumentException("PaymentIntent not found or missing metadata");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("PaymentIntent not found or missing metadata");
        }
    }

    public Page<OrderDetails> getOrdersByUser(Principal principal, Pageable pageable) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = jwt.getClaim("sub");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.USER_NOT_EXISTED));

        Page<Order> ordersPage = orderRepository.findByUser(user, pageable);

        return ordersPage.map(order -> OrderDetails.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatus())
                .paymentMethod(order.getPaymentMethod())
                .shipmentNumber(order.getShipmentTrackingNumber())
                .address(order.getAddress())
                .totalAmount(order.getTotalAmount())
                .totalPrice(order.getTotalPrice())
                .orderItemList(getItemDetails(order.getOrderItemList()))
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .build());
    }


    public OrderDetails getOrderDetails(String orderId) {
        Order order =
                orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.ORDER_NOT_EXISTED));
        return OrderDetails.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatus())
                .paymentMethod(order.getPaymentMethod())
                .shipmentNumber(order.getShipmentTrackingNumber())
                .address(order.getAddress())
                .totalPrice(order.getTotalPrice())
                .totalAmount(order.getTotalAmount())
                .orderItemList(getItemDetails(order.getOrderItemList()))
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .build();
    }

    private List<OrderItemDetail> getItemDetails(List<OrderItem> orderItemList) {

        return orderItemList.stream().map(orderItem -> OrderItemDetail.builder()
                .id(orderItem.getId())
                .itemPrice(orderItem.getItemPrice())
                .product(orderItem.getProduct())
                .productVariantId(orderItem.getProductVariantId())
                .quantity(orderItem.getQuantity())
                .build()).toList();
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public OrderDetails updateOrderStatus(String orderId, OrderStatus newStatus) {

        // Tìm đơn hàng theo ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.ORDER_NOT_EXISTED));

        // Cập nhật trạng thái đơn hàng
        order.setOrderStatus(newStatus);

        // Xử lý trạng thái thanh toán dựa trên trạng thái đơn hàng
        if (newStatus == OrderStatus.DELIVERED) {
            order.getPayment().setPaymentStatus(PaymentStatus.COMPLETED);
        } else if (newStatus == OrderStatus.CANCELLED) {
            order.getPayment().setPaymentStatus(PaymentStatus.FAILED);

        }
        // Lưu vào cơ sở dữ liệu
        Order savedOrder = orderRepository.save(order);
//        notificationService.sendOrderStatusUpdateNotification(order, newStatus);



        // Trả về thông tin đơn hàng sau khi cập nhật
        return OrderDetails.builder()
                .id(savedOrder.getId())
                .orderDate(savedOrder.getOrderDate())
                .orderStatus(savedOrder.getOrderStatus())
                .paymentMethod(savedOrder.getPaymentMethod())
                .shipmentNumber(savedOrder.getShipmentTrackingNumber())
                .address(savedOrder.getAddress())
                .totalPrice(savedOrder.getTotalPrice())
                .totalAmount(savedOrder.getTotalAmount())
                .orderItemList(getItemDetails(savedOrder.getOrderItemList()))
                .expectedDeliveryDate(savedOrder.getExpectedDeliveryDate())
                .build();
    }


    public void cancelOrder(String id, Principal principal) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = jwt.getClaim("sub");
        User user =
                userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.USER_NOT_EXISTED));        Order order = orderRepository.findById(id).get();
        if (order.getUser().getId().equals(user.getId())) {
            order.setOrderStatus(OrderStatus.CANCELLED);
            //logic to refund amount
            orderRepository.save(order);
        } else {
            throw new RuntimeException("Invalid request");
        }

    }
}