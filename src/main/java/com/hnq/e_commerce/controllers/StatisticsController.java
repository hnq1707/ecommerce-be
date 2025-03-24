package com.hnq.e_commerce.controllers;

import com.hnq.e_commerce.dto.CategoryDataDTO;
import com.hnq.e_commerce.dto.DashboardStatsDTO;
import com.hnq.e_commerce.dto.OrderDataDTO;
import com.hnq.e_commerce.dto.RevenueDataDTO;
import com.hnq.e_commerce.services.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistic")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * Lấy dữ liệu tổng quan thống kê
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        DashboardStatsDTO stats = statisticsService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Lấy dữ liệu doanh thu theo tháng
     */
    @GetMapping("/revenue")
    public ResponseEntity<List<RevenueDataDTO>> getRevenueData() {
        List<RevenueDataDTO> revenueData = statisticsService.getRevenueData();
        return ResponseEntity.ok(revenueData);
    }

    /**
     * Lấy dữ liệu số lượng đơn hàng theo tháng
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderDataDTO>> getOrderData() {
        List<OrderDataDTO> orderData = statisticsService.getOrderData();
        return ResponseEntity.ok(orderData);
    }

    /**
     * Lấy dữ liệu thống kê theo danh mục sản phẩm
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDataDTO>> getCategoryData() {
        List<CategoryDataDTO> categoryData = statisticsService.getCategoryData();
        return ResponseEntity.ok(categoryData);
    }
}
