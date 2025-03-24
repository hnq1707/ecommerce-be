package com.hnq.e_commerce.services;

import com.hnq.e_commerce.auth.exceptions.ErrorCode;
import com.hnq.e_commerce.entities.*;
import com.hnq.e_commerce.exception.ResourceNotFoundEx;
import com.hnq.e_commerce.repositories.InvoiceRepository;
import com.hnq.e_commerce.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;

    // Lấy danh sách hóa đơn có phân trang
    public Page<Invoice> getAllInvoices(Pageable pageable) {
        return invoiceRepository.findAll(pageable);
    }

    // Lấy danh sách hóa đơn đã thanh toán có phân trang
    public Page<Invoice> getPaidInvoices(Pageable pageable) {
        return invoiceRepository.findByIsPaid(true, pageable);
    }

    // Lấy danh sách hóa đơn chưa thanh toán có phân trang
    public Page<Invoice> getUnpaidInvoices(Pageable pageable) {
        return invoiceRepository.findByIsPaid(false, pageable);
    }

    // Tạo hóa đơn từ đơn hàng
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Invoice createInvoiceFromOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new RuntimeException("Order is not delivered yet");
        }
        Optional<Invoice> existingInvoice = invoiceRepository.findByOrder(order);
        if (existingInvoice.isPresent()) {

            throw new ResourceNotFoundEx(ErrorCode.ORDER_EXISTED);
        }
        boolean isPaid =
                order.getPayment() != null && order.getPayment().getPaymentStatus() == PaymentStatus.COMPLETED;

        Invoice invoice = Invoice.builder()
                .order(order)
                .totalAmount(order.getTotalAmount())
                .totalPrice(order.getTotalPrice())
                .issuedDate(new Date())
                .billingAddress(order.getAddress())
                .isPaid(isPaid)
                .build();

        invoiceRepository.save(invoice);
        return invoice;
    }

    // Tạo hóa đơn cho tất cả đơn hàng DELIVERED chưa có hóa đơn
    public List<Invoice> generateInvoicesForDeliveredOrders() {
        List<Order> deliveredOrders = orderRepository.findByOrderStatus(OrderStatus.DELIVERED);

        List<Invoice> invoices = deliveredOrders.stream()
                .filter(order -> invoiceRepository.findById(order.getId()).isEmpty()) // Chỉ tạo hóa đơn mới
                .map(order -> {
                    boolean isPaid =
                            order.getPayment() != null && order.getPayment().getPaymentStatus() == PaymentStatus.COMPLETED;
                    return Invoice.builder()
                            .order(order)
                            .totalAmount(order.getTotalAmount())
                            .totalPrice(order.getTotalPrice())
                            .issuedDate(new Date())
                            .billingAddress(order.getAddress())
                            .isPaid(isPaid)
                            .build();
                })
                .collect(Collectors.toList());

        return invoiceRepository.saveAll(invoices);
    }
}
