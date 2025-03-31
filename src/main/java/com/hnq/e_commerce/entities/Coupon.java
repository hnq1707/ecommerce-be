package com.hnq.e_commerce.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank(message = "Mã coupon không được để trống")
    @Column(unique = true)
    private String code;

    private String description;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @NotNull(message = "Giá trị giảm giá không được để trống")
    private BigDecimal discountValue;

    private BigDecimal minimumPurchaseAmount;

    private BigDecimal maximumDiscountAmount;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime endDate;

    private boolean isActive = true;

    private Integer usageLimit;

    private Integer usageCount = 0;

    // Thêm trường để xác định loại coupon
    @Enumerated(EnumType.STRING)
    private CouponType couponType = CouponType.GENERAL;

    // Mối quan hệ với sản phẩm - chỉ lưu ID
    @ElementCollection
    @CollectionTable(
            name = "coupon_product_ids",
            joinColumns = @JoinColumn(name = "coupon_id")
    )
    @Column(name = "product_id")
    private Set<String> productIds = new HashSet<>();

    // Mối quan hệ với danh mục - chỉ lưu ID
    @ElementCollection
    @CollectionTable(
            name = "coupon_category_ids",
            joinColumns = @JoinColumn(name = "coupon_id")
    )
    @Column(name = "category_id")
    private Set<String> categoryIds = new HashSet<>();

    // Mối quan hệ với người dùng - chỉ lưu ID
    @ElementCollection
    @CollectionTable(
            name = "coupon_user_ids",
            joinColumns = @JoinColumn(name = "coupon_id")
    )
    @Column(name = "user_id")
    private Set<String> userIds = new HashSet<>();

    public enum DiscountType {
        PERCENTAGE, // Giảm giá theo phần trăm
        FIXED_AMOUNT // Giảm giá theo số tiền cố định
    }

    public enum CouponType {
        GENERAL,     // Coupon áp dụng cho tất cả
        PRODUCT,     // Coupon áp dụng cho sản phẩm cụ thể
        CATEGORY,    // Coupon áp dụng cho danh mục cụ thể
        USER         // Coupon áp dụng cho người dùng cụ thể
    }

    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive &&
                now.isAfter(startDate) &&
                now.isBefore(endDate) &&
                (usageLimit == null || usageCount < usageLimit);
    }

    // Kiểm tra xem coupon có áp dụng cho sản phẩm không
    public boolean isApplicableToProduct(String productId) {
        if (couponType == CouponType.GENERAL) {
            return true;
        }

        if (couponType == CouponType.PRODUCT) {
            return productIds.contains(productId);
        }

        return false;
    }

    // Kiểm tra xem coupon có áp dụng cho danh mục không
    public boolean isApplicableToCategory(String categoryId) {
        if (couponType == CouponType.GENERAL) {
            return true;
        }

        if (couponType == CouponType.CATEGORY) {
            return categoryIds.contains(categoryId);
        }

        return false;
    }

    // Kiểm tra xem coupon có áp dụng cho người dùng không
    public boolean isApplicableToUser(String userId) {
        if (couponType == CouponType.GENERAL) {
            return true;
        }

        if (couponType == CouponType.USER) {
            return userIds.contains(userId);
        }

        return false;
    }
}

