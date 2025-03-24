package com.hnq.e_commerce.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDTO {
    private long totalRevenue;
    private int totalOrders;
    private int totalCustomers;
    private int averageOrderValue;
    private double revenueGrowth;
    private double ordersGrowth;
    private double customersGrowth;
    private double aovGrowth;
}
