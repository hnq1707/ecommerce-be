package com.hnq.e_commerce.controllers;

import com.hnq.e_commerce.dto.ApiResponse;
import com.hnq.e_commerce.entities.Invoice;
import com.hnq.e_commerce.services.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public ApiResponse<Page<Invoice>> getAllInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.<Page<Invoice>>builder().result(invoiceService.getAllInvoices(PageRequest.of(page, size))).build();
    }

    @GetMapping("/paid")
    public ApiResponse<Page<Invoice>> getPaidInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.<Page<Invoice>> builder().result(invoiceService.getPaidInvoices(PageRequest.of(page,
                                                                                          size))).build();
    }

    @GetMapping("/unpaid")
    public ApiResponse<Page<Invoice>> getUnpaidInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.<Page<Invoice>>builder().result(invoiceService.getUnpaidInvoices(PageRequest.of(page,
                                                                                            size))).build();
    }


    @PostMapping
    public ApiResponse<Invoice> createInvoice(@RequestParam String orderId) {
        return ApiResponse.<Invoice>builder().result(invoiceService.createInvoiceFromOrder(orderId)).build();
    }
}
