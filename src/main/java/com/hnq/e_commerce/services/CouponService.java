package com.hnq.e_commerce.services;




import com.hnq.e_commerce.auth.exceptions.ErrorCode;
import com.hnq.e_commerce.auth.repositories.UserRepository;
import com.hnq.e_commerce.entities.Coupon;
import com.hnq.e_commerce.exception.ResourceNotFoundEx;
import com.hnq.e_commerce.repositories.CategoryRepository;
import com.hnq.e_commerce.repositories.CouponRepository;
import com.hnq.e_commerce.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Autowired
    public CouponService(CouponRepository couponRepository,
                         ProductRepository productRepository,
                         CategoryRepository categoryRepository,
                         UserRepository userRepository) {
        this.couponRepository = couponRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    public Coupon getCouponById(String id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.COUPON_NOT_EXISTED));
    }

    public Coupon getCouponByCode(String code) {
        return couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundEx(ErrorCode.COUPON_NOT_EXISTED));
    }

    public List<Coupon> getActiveCoupons() {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findByEndDateAfterAndStartDateBeforeAndIsActiveTrue(now, now);
    }

    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    @Transactional
    public Coupon updateCoupon(String id, Coupon couponDetails) {
        Coupon coupon = getCouponById(id);

        coupon.setCode(couponDetails.getCode());
        coupon.setDescription(couponDetails.getDescription());
        coupon.setDiscountType(couponDetails.getDiscountType());
        coupon.setDiscountValue(couponDetails.getDiscountValue());
        coupon.setMinimumPurchaseAmount(couponDetails.getMinimumPurchaseAmount());
        coupon.setMaximumDiscountAmount(couponDetails.getMaximumDiscountAmount());
        coupon.setStartDate(couponDetails.getStartDate());
        coupon.setEndDate(couponDetails.getEndDate());
        coupon.setActive(couponDetails.isActive());
        coupon.setUsageLimit(couponDetails.getUsageLimit());
        coupon.setCouponType(couponDetails.getCouponType());

        return couponRepository.save(coupon);
    }

    @Transactional
    public void deleteCoupon(String id) {
        Coupon coupon = getCouponById(id);
        couponRepository.delete(coupon);
    }

    public BigDecimal calculateDiscount(String couponCode, BigDecimal orderAmount) {
        Coupon coupon = getCouponByCode(couponCode);

        if (!coupon.isValid()) {
            throw new IllegalArgumentException("Coupon không hợp lệ hoặc đã hết hạn");
        }

        if (coupon.getMinimumPurchaseAmount() != null &&
                orderAmount.compareTo(coupon.getMinimumPurchaseAmount()) < 0) {
            throw new IllegalArgumentException("Giá trị đơn hàng không đủ để áp dụng coupon này");
        }

        BigDecimal discountAmount;

        if (coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
            discountAmount = orderAmount.multiply(coupon.getDiscountValue().divide(new BigDecimal("100")));
        } else {
            discountAmount = coupon.getDiscountValue();
        }

        if (coupon.getMaximumDiscountAmount() != null &&
                discountAmount.compareTo(coupon.getMaximumDiscountAmount()) > 0) {
            discountAmount = coupon.getMaximumDiscountAmount();
        }

        return discountAmount;
    }

    @Transactional
    public void applyCoupon(String couponCode) {
        Coupon coupon = getCouponByCode(couponCode);

        if (!coupon.isValid()) {
            throw new IllegalArgumentException("Coupon không hợp lệ hoặc đã hết hạn");
        }

        coupon.setUsageCount(coupon.getUsageCount() + 1);
        couponRepository.save(coupon);
    }

    // Phương thức mới: Thêm sản phẩm vào coupon
    @Transactional
    public Coupon addProductToCoupon(String couponId, String productId) {
        Coupon coupon = getCouponById(couponId);

        // Kiểm tra sản phẩm có tồn tại không
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundEx(ErrorCode.PRODUCT_NOT_FOUND);
        }

        coupon.getProductIds().add(productId);
        coupon.setCouponType(Coupon.CouponType.PRODUCT);
        return couponRepository.save(coupon);
    }

    // Phương thức mới: Thêm danh mục vào coupon
    @Transactional
    public Coupon addCategoryToCoupon(String couponId, String categoryId) {
        Coupon coupon = getCouponById(couponId);

        // Kiểm tra danh mục có tồn tại không
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundEx(ErrorCode.CATEGORY_NOT_FOUND);
        }

        coupon.getCategoryIds().add(categoryId);
        coupon.setCouponType(Coupon.CouponType.CATEGORY);
        return couponRepository.save(coupon);
    }

    // Phương thức mới: Thêm người dùng vào coupon
    @Transactional
    public Coupon addUserToCoupon(String couponId, String userId) {
        Coupon coupon = getCouponById(couponId);

        // Kiểm tra người dùng có tồn tại không
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundEx(ErrorCode.USER_NOT_EXISTED);
        }

        coupon.getUserIds().add(userId);
        coupon.setCouponType(Coupon.CouponType.USER);
        return couponRepository.save(coupon);
    }

    // Phương thức mới: Xóa sản phẩm khỏi coupon
    @Transactional
    public Coupon removeProductFromCoupon(String couponId, String productId) {
        Coupon coupon = getCouponById(couponId);
        coupon.getProductIds().remove(productId);

        if (coupon.getProductIds().isEmpty() && coupon.getCouponType() == Coupon.CouponType.PRODUCT) {
            coupon.setCouponType(Coupon.CouponType.GENERAL);
        }

        return couponRepository.save(coupon);
    }

    // Phương thức mới: Xóa danh mục khỏi coupon
    @Transactional
    public Coupon removeCategoryFromCoupon(String couponId, String categoryId) {
        Coupon coupon = getCouponById(couponId);
        coupon.getCategoryIds().remove(categoryId);

        if (coupon.getCategoryIds().isEmpty() && coupon.getCouponType() == Coupon.CouponType.CATEGORY) {
            coupon.setCouponType(Coupon.CouponType.GENERAL);
        }

        return couponRepository.save(coupon);
    }

    // Phương thức mới: Xóa người dùng khỏi coupon
    @Transactional
    public Coupon removeUserFromCoupon(String couponId, String userId) {
        Coupon coupon = getCouponById(couponId);
        coupon.getUserIds().remove(userId);

        if (coupon.getUserIds().isEmpty() && coupon.getCouponType() == Coupon.CouponType.USER) {
            coupon.setCouponType(Coupon.CouponType.GENERAL);
        }

        return couponRepository.save(coupon);
    }

    // Phương thức mới: Lấy tất cả coupon áp dụng cho sản phẩm
    public List<Coupon> getCouponsByProductId(String productId) {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findValidCouponsByProductId(productId, now);
    }

    // Phương thức mới: Lấy tất cả coupon áp dụng cho danh mục
    public List<Coupon> getCouponsByCategoryId(String categoryId) {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findValidCouponsByCategoryId(categoryId, now);
    }

    // Phương thức mới: Lấy tất cả coupon áp dụng cho người dùng
    public List<Coupon> getCouponsByUserId(String userId) {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findValidCouponsByUserId(userId, now);
    }

    // Phương thức mới: Kiểm tra coupon có áp dụng được cho sản phẩm không
    public boolean isCouponValidForProduct(String couponCode, String productId) {
        Coupon coupon = getCouponByCode(couponCode);
        return coupon.isValid() && coupon.isApplicableToProduct(productId);
    }

    // Phương thức mới: Kiểm tra coupon có áp dụng được cho danh mục không
    public boolean isCouponValidForCategory(String couponCode, String categoryId) {
        Coupon coupon = getCouponByCode(couponCode);
        return coupon.isValid() && coupon.isApplicableToCategory(categoryId);
    }

    // Phương thức mới: Kiểm tra coupon có áp dụng được cho người dùng không
    public boolean isCouponValidForUser(String couponCode, String userId) {
        Coupon coupon = getCouponByCode(couponCode);
        return coupon.isValid() && coupon.isApplicableToUser(userId);
    }
}

