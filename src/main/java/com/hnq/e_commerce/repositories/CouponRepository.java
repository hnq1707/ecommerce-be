package com.hnq.e_commerce.repositories;

import com.hnq.e_commerce.entities.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, String> {

    Optional<Coupon> findByCode(String code);

    List<Coupon> findByIsActiveTrue();

    List<Coupon> findByEndDateAfterAndStartDateBeforeAndIsActiveTrue(
            LocalDateTime currentDate, LocalDateTime currentDate2);

    // Tìm coupon áp dụng cho sản phẩm cụ thể
    @Query("SELECT c FROM Coupon c WHERE :productId MEMBER OF c.productIds AND c.isActive = true " +
            "AND c.startDate <= :currentDate AND c.endDate >= :currentDate")
    List<Coupon> findValidCouponsByProductId(@Param("productId") String productId,
                                             @Param("currentDate") LocalDateTime currentDate);

    // Tìm coupon áp dụng cho danh mục cụ thể
    @Query("SELECT c FROM Coupon c WHERE :categoryId MEMBER OF c.categoryIds AND c.isActive = true " +
            "AND c.startDate <= :currentDate AND c.endDate >= :currentDate")
    List<Coupon> findValidCouponsByCategoryId(@Param("categoryId") String categoryId,
                                              @Param("currentDate") LocalDateTime currentDate);

    // Tìm coupon áp dụng cho người dùng cụ thể
    @Query("SELECT c FROM Coupon c WHERE :userId MEMBER OF c.userIds AND c.isActive = true " +
            "AND c.startDate <= :currentDate AND c.endDate >= :currentDate")
    List<Coupon> findValidCouponsByUserId(@Param("userId") String userId,
                                          @Param("currentDate") LocalDateTime currentDate);
}

