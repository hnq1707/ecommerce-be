package com.hnq.e_commerce.services;


import com.hnq.e_commerce.auth.services.UserService;
import com.hnq.e_commerce.dto.CategoryDataDTO;
import com.hnq.e_commerce.dto.DashboardStatsDTO;
import com.hnq.e_commerce.dto.OrderDataDTO;
import com.hnq.e_commerce.dto.RevenueDataDTO;
import com.hnq.e_commerce.dto.ProductDto;
import com.hnq.e_commerce.entities.Category;
import com.hnq.e_commerce.dto.OrderDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final OrderService orderService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final ProductService productService;

    /**
     * Tổng hợp các số liệu thống kê chính:
     * - Tổng doanh thu (sum của totalAmount của tất cả đơn hàng)
     * - Số đơn hàng
     * - Số khách hàng (lấy từ UserService)
     * - Giá trị trung bình của đơn hàng
     * <p>
     * Các chỉ số tăng trưởng được giả định là 0 (cần logic cụ thể dựa vào dữ liệu lịch sử).
     */
    public DashboardStatsDTO getDashboardStats() {
        Page<OrderDetails> ordersPage = orderService.getAllOrders(Pageable.unpaged());
        List<OrderDetails> orders = ordersPage.getContent();
        long totalOrders = orders.size();
        long totalRevenue = orders.stream()
                .map(OrderDetails::getTotalPrice)
                .mapToLong(Double::longValue)
                .sum();

        int totalCustomers = userService.getUsers().size();
        long avgOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;

        // Các chỉ số tăng trưởng được tính toán cụ thể nếu có dữ liệu lịch sử, ở đây dùng giá trị 0
        double revenueGrowth = 0;
        double ordersGrowth = 0;
        double customersGrowth = 0;
        double aovGrowth = 0;

        return new DashboardStatsDTO(
                totalRevenue, (int) totalOrders, totalCustomers, (int) avgOrderValue,
                revenueGrowth, ordersGrowth, customersGrowth, aovGrowth
        );
    }

    /**
     * Nhóm đơn hàng theo tháng (T1 - T12) dựa vào orderDate để tính doanh thu từng tháng.
     */
    public List<RevenueDataDTO> getRevenueData() {
        Page<OrderDetails> ordersPage = orderService.getAllOrders(Pageable.unpaged());
        List<OrderDetails> orders = ordersPage.getContent();
        Map<Integer, Long> revenueByMonth = new HashMap<>();
        for (OrderDetails order : orders) {
            // Giả định orderDate là kiểu java.util.Date
            Calendar cal = Calendar.getInstance();
            cal.setTime(order.getOrderDate());
            int month = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH là 0-indexed
            revenueByMonth.put(month,
                               (long) (revenueByMonth.getOrDefault(month, 0L) + order.getTotalPrice()));
        }
        List<RevenueDataDTO> revenueData = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            revenueData.add(new RevenueDataDTO("T" + i, revenueByMonth.getOrDefault(i, 0L)));
        }
        return revenueData;
    }

    /**
     * Nhóm đơn hàng theo tháng (T1 - T12) để tính số lượng đơn hàng mỗi tháng.
     */
    public List<OrderDataDTO> getOrderData() {
        Page<OrderDetails> ordersPage = orderService.getAllOrders(Pageable.unpaged());
        List<OrderDetails> orders = ordersPage.getContent();
        Map<Integer, Integer> ordersByMonth = new HashMap<>();
        for (OrderDetails order : orders) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(order.getOrderDate());
            int month = cal.get(Calendar.MONTH) + 1;
            ordersByMonth.put(month, ordersByMonth.getOrDefault(month, 0) + 1);
        }
        List<OrderDataDTO> orderData = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            orderData.add(new OrderDataDTO("T" + i, ordersByMonth.getOrDefault(i, 0)));
        }
        return orderData;
    }

    /**
     * Lấy dữ liệu thống kê theo danh mục: tên danh mục và số lượng sản phẩm thuộc danh mục đó.
     */
    public List<CategoryDataDTO> getCategoryData() {
        List<Category> categories = categoryService.getAllCategory();
        List<CategoryDataDTO> categoryData = new ArrayList<>();
        for (Category category : categories) {
            // Gọi ProductService để lấy tất cả sản phẩm theo categoryId (không phân trang)
            Page<ProductDto> productPage = productService.getAllProducts(category.getId(), null, Pageable.unpaged());
            int productCount = (int) productPage.getTotalElements();
            // Ví dụ: sử dụng màu cố định (có thể cải tiến để tính toán màu dựa trên số liệu)
            categoryData.add(new CategoryDataDTO(category.getName(), productCount, "hsl(215,70%,50%)"));
        }
        return categoryData;
    }
}
