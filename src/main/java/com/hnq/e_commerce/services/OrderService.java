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
import com.hnq.e_commerce.repositories.ProductRepository;
import com.stripe.model.PaymentIntent;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class OrderService {


    OrderRepository orderRepository;


    ProductService productService;


    PaymentIntentService paymentIntentService;
   UserRepository userRepository;
   EmailService emailService;
      ProductRepository productRepository;
    private final InvoiceService invoiceService;
    NotificationService notificationService;


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
                        // Bắt buộc phải có productVariantId (client đã đảm bảo không cho đặt hàng nếu không có)
                        if (orderItemRequest.getProductVariantId() == null || orderItemRequest.getProductVariantId().isEmpty()) {
                            throw new Exception("Product variant is required for ordering.");
                        }

                        // Lấy thông tin sản phẩm dựa trên productId
                        Product product = productService.fetchProductById(orderItemRequest.getProductId());

                        // Tìm variant trong danh sách productVariants của Product
                        ProductVariant variant = product.getProductVariants().stream()
                                .filter(v -> v.getId().equals(orderItemRequest.getProductVariantId()))
                                .findFirst()
                                .orElseThrow(() -> new Exception("Product variant not found with id: " + orderItemRequest.getProductVariantId()));

                        // Kiểm tra tồn kho của variant
                        if (variant.getStockQuantity() < orderItemRequest.getQuantity()) {
                            throw new Exception("Insufficient stock for product variant id: " + variant.getId());
                        }

                        // Cập nhật tồn kho của variant
                        variant.setStockQuantity(variant.getStockQuantity() - orderItemRequest.getQuantity());
                        // Lưu lại Product để cập nhật danh sách variant đã thay đổi
                        productRepository.save(product);

                        // Tạo đối tượng OrderItem
                        return OrderItem.builder()
                                .product(product)
                                .productVariantId(variant.getId())
                                .quantity(orderItemRequest.getQuantity())
                                .itemPrice(orderItemRequest.getPrice())
                                .order(order)
                                .build();
                    } catch (Exception e) {
                        log.error("Error processing order item. Product ID: {}, Variant ID: {}, Error: {}",
                                  orderItemRequest.getProductId(), orderItemRequest.getProductVariantId(), e.getMessage(), e);
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
        notificationService.createNotification(
            order.getUser().getId(),
            "Đơn hàng mới",
            "Đơn hàng #" + savedOrder.getId() + " của bạn đã được tạo thành công.",
            NotificationType.ORDER,
            "/orders/" + savedOrder.getId(),
            null,
            null,
            null
        );

        notificationService.notifyNewOrder(
                savedOrder.getId(),
                savedOrder.getId(),
                savedOrder.getUser().getId(),
                savedOrder.getUser().getEmail(),
                savedOrder.getTotalAmount()
        );


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

                notificationService.createNotification(
                        payment.getOrder().getUser().getId(),
                        "Thanh toán thành công",
                        "Thanh toán cho đơn hàng #" + payment.getOrder().getId() + " đã được xử lý " +
                                "thành " +
                                "công với số tiền "
                                + payment.getAmount(),
                        NotificationType.PAYMENT,
                        "/payments/" + payment.getId(),
                        null,
                        null,
                        null
                );


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
    @Transactional
    public OrderDetails updateOrderStatus(String orderId, OrderStatus newStatus) {

        // Tìm đơn hàng theo ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.ORDER_NOT_EXISTED));

        // Cập nhật trạng thái đơn hàng
        order.setOrderStatus(newStatus);

        // Xử lý trạng thái thanh toán dựa trên trạng thái đơn hàng
        if (newStatus == OrderStatus.DELIVERED) {
            invoiceService.createInvoiceFromOrder(orderId);
            order.getPayment().setPaymentStatus(PaymentStatus.COMPLETED);

        } else if (newStatus == OrderStatus.CANCELLED) {
            order.getPayment().setPaymentStatus(PaymentStatus.FAILED);

        }
        // Lưu vào cơ sở dữ liệu
        Order savedOrder = orderRepository.save(order);
        String message = "";
            switch (newStatus) {
                case IN_PROGRESS:
                    message = "Đơn hàng #" + order.getId() + " đã được xác nhận.";
                    break;
                case SHIPPED:
                    message = "Đơn hàng #" + order.getId() + " đã được giao cho đơn vị vận chuyển.";
                    break;
                case DELIVERED:
                    message = "Đơn hàng #" + order.getId() + " đã được giao thành công.";
                    break;
                case CANCELLED:
                    message = "Đơn hàng #" + order.getId() + " đã bị hủy.";
                    notificationService.notifyCancelledOrder(
                            order.getId(),
                            order.getId(),
                            order.getUser().getId(),
                            order.getUser().getEmail(),
                            null
                    );
                    break;
                default:
                    message =
                            "Trạng thái đơn hàng #" + order.getId() + " đã được cập nhật thành " + newStatus;
            }

            notificationService.createNotification(
                order.getUser().getId(),
                "Cập nhật đơn hàng",
                message,
                NotificationType.ORDER,
                "/orders/" + orderId,
                null,
                null,
                null
            );



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

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Page<OrderDetails> getAllOrders(Pageable pageable) {
        // Lấy danh sách đơn hàng từ repository với phân trang
        Page<Order> ordersPage = orderRepository.findAll(pageable);

        // Map đối tượng Order sang OrderDetails để trả về thông tin chi tiết
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


    public void cancelOrder(String id, Principal principal) throws Exception {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = jwt.getClaim("sub");
        User user =
                userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.USER_NOT_EXISTED));        Order order = orderRepository.findById(id).get();
        if (order.getUser().getId().equals(user.getId())) {
            order.setOrderStatus(OrderStatus.CANCELLED);
            for (OrderItem item : order.getOrderItemList()) {
                // Lấy thông tin sản phẩm dựa trên productId
                Product product = productService.fetchProductById(item.getId());
                // Tìm variant trong danh sách productVariants của Product
                ProductVariant variant = product.getProductVariants().stream()
                        .filter(v -> v.getId().equals(item.getProductVariantId()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.PRODUCT_NOT_FOUND));

                // Cập nhật tồn kho của variant
                variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
            orderRepository.save(order);
        } else {
            throw new RuntimeException("Invalid request");
        }

    }
}