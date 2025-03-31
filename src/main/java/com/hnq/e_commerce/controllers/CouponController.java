package com.hnq.e_commerce.controllers;

import com.hnq.e_commerce.dto.ApiResponse;
import com.hnq.e_commerce.dto.CouponValidationRequest;
import com.hnq.e_commerce.dto.DiscountRequest;
import com.hnq.e_commerce.dto.DiscountResponse;
import com.hnq.e_commerce.entities.Coupon;
import com.hnq.e_commerce.services.CouponService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;

    @Autowired
    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping
    public ApiResponse<List<Coupon>> getAllCoupons() {
        return ApiResponse.<List<Coupon>>builder().result(couponService.getAllCoupons()).build();
    }

    @GetMapping("/active")
    public ApiResponse<List<Coupon>> getActiveCoupons() {
        return ApiResponse.<List<Coupon>>builder().result(couponService.getActiveCoupons()).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<Coupon> getCouponById(@PathVariable String id) {
        return ApiResponse.<Coupon>builder().result(couponService.getCouponById(id)).build();
    }

    @GetMapping("/code/{code}")
    public ApiResponse<Coupon> getCouponByCode(@PathVariable String code) {
        return ApiResponse.<Coupon>builder().result(couponService.getCouponByCode(code)).build();
    }

    @PostMapping
    public ApiResponse<Coupon> createCoupon(@Valid @RequestBody Coupon coupon) {
        return ApiResponse.<Coupon>builder().result(couponService.createCoupon(coupon)).build();
    }

    @PutMapping("/{id}")
    public ApiResponse<Coupon> updateCoupon(@PathVariable String id,
                                               @Valid @RequestBody Coupon coupon) {
        return ApiResponse.<Coupon>builder().result(couponService.updateCoupon(id, coupon)).build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCoupon(@PathVariable String id) {
        couponService.deleteCoupon(id);
        return ApiResponse.<Void>builder().result(null).build();
    }

    @PostMapping("/validate")
    public ApiResponse<DiscountResponse> validateCoupon(@RequestBody DiscountRequest request) {
        try {
            BigDecimal discount = couponService.calculateDiscount(request.getCouponCode(), request.getOrderAmount());
            DiscountResponse response = new DiscountResponse(true, "Coupon hợp lệ", discount);
            return ApiResponse.<DiscountResponse>builder().result(response).build();
        } catch (Exception e) {
            DiscountResponse response = new DiscountResponse(false, e.getMessage(), BigDecimal.ZERO);
            return ApiResponse.<DiscountResponse>builder().result(response).build();
        }
    }

    @PostMapping("/apply")
    public ApiResponse<Void> applyCoupon(@RequestParam String code) {
        couponService.applyCoupon(code);
        return ApiResponse.<Void>builder().result(null).build();
    }

    // Endpoint mới: Thêm sản phẩm vào coupon
    @PostMapping("/{couponId}/products/{productId}")
    public ApiResponse<Coupon> addProductToCoupon(@PathVariable String couponId,
                                                     @PathVariable String productId) {
        return ApiResponse.<Coupon>builder().result(couponService.addProductToCoupon(couponId,productId)).build();
    }

    // Endpoint mới: Thêm danh mục vào coupon
    @PostMapping("/{couponId}/categories/{categoryId}")
    public ApiResponse<Coupon> addCategoryToCoupon(@PathVariable String couponId,
                                         @PathVariable String categoryId) {
        return ApiResponse.<Coupon>builder().result(couponService.addCategoryToCoupon(couponId,categoryId)).build();
    }

    // Endpoint mới: Thêm người dùng vào coupon
    @PostMapping("/{couponId}/users/{userId}")
    public ApiResponse<Coupon> addUserToCoupon(@PathVariable String couponId,
                                               @PathVariable String userId) {
        return ApiResponse.<Coupon>builder().result(couponService.addUserToCoupon(couponId,
                                                                                      userId)).build();
    }

    // Endpoint mới: Xóa sản phẩm khỏi coupon
    @DeleteMapping("/{couponId}/products/{productId}")
    public ApiResponse<Coupon> removeProductFromCoupon(@PathVariable String couponId,
                                             @PathVariable String productId) {
        return ApiResponse.<Coupon>builder().result(couponService.removeProductFromCoupon(couponId,productId)).build();
    }

    // Endpoint mới: Xóa danh mục khỏi coupon
    @DeleteMapping("/{couponId}/categories/{categoryId}")
    public ApiResponse<Coupon> removeCategoryFromCoupon(@PathVariable String couponId,
                                              @PathVariable String categoryId) {
        return ApiResponse.<Coupon>builder().result(couponService.removeCategoryFromCoupon(couponId,categoryId)).build();
    }

    // Endpoint mới: Xóa người dùng khỏi coupon
    @DeleteMapping("/{couponId}/users/{userId}")
    public ApiResponse<Coupon> removeUserFromCoupon(@PathVariable String couponId,
                                                    @PathVariable String userId) {
        return ApiResponse.<Coupon>builder().result(couponService.removeUserFromCoupon(couponId,
                                                                                       userId)).build();
    }

    // Endpoint mới: Lấy tất cả coupon áp dụng cho sản phẩm
    @GetMapping("/products/{productId}")
    public ApiResponse<List<Coupon>> getCouponsByProductId(@PathVariable String productId) {
        return ApiResponse.<List<Coupon>>builder().result(couponService.getCouponsByProductId(productId)).build();
    }

    // Endpoint mới: Lấy tất cả coupon áp dụng cho danh mục
    @GetMapping("/categories/{categoryId}")
    public ApiResponse<List<Coupon>> getCouponsByCategoryId(@PathVariable String categoryId) {
        return ApiResponse.<List<Coupon>>builder().result(couponService.getCouponsByCategoryId(categoryId)).build();
    }

    // Endpoint mới: Lấy tất cả coupon áp dụng cho người dùng
    @GetMapping("/users/{userId}")
    public ApiResponse<List<Coupon>> getCouponsByUserId(@PathVariable String userId) {
        return ApiResponse.<List<Coupon>>builder().result(couponService.getCouponsByUserId(userId)).build();
    }

    // Endpoint mới: Kiểm tra coupon có áp dụng được cho sản phẩm không
    @PostMapping("/validate/product")
    public ApiResponse<Boolean> validateCouponForProduct(@RequestBody CouponValidationRequest request) {
        boolean isValid = couponService.isCouponValidForProduct(request.getCouponCode(), request.getEntityId());
        return ApiResponse.<Boolean>builder().result(isValid).build();
    }

    // Endpoint mới: Kiểm tra coupon có áp dụng được cho danh mục không
    @PostMapping("/validate/category")
    public ApiResponse<Boolean> validateCouponForCategory(@RequestBody CouponValidationRequest request) {
        boolean isValid = couponService.isCouponValidForCategory(request.getCouponCode(), request.getEntityId());
        return ApiResponse.<Boolean>builder().result(isValid).build();
    }

    // Endpoint mới: Kiểm tra coupon có áp dụng được cho người dùng không
    @PostMapping("/validate/user")
    public ApiResponse<Boolean> validateCouponForUser(@RequestBody CouponValidationRequest request) {
        boolean isValid = couponService.isCouponValidForUser(request.getCouponCode(), request.getEntityId());
        return ApiResponse.<Boolean>builder().result(isValid).build();
    }
}

