package com.hnq.e_commerce.services;

import com.hnq.e_commerce.auth.entities.User;
import com.hnq.e_commerce.auth.repositories.UserRepository;
import com.hnq.e_commerce.dto.request.ReviewRequest;
import com.hnq.e_commerce.dto.response.RatingStatisticsResponse;
import com.hnq.e_commerce.dto.response.ReviewResponse;
import com.hnq.e_commerce.entities.Order;
import com.hnq.e_commerce.entities.OrderStatus;
import com.hnq.e_commerce.entities.Product;
import com.hnq.e_commerce.entities.Review;
import com.hnq.e_commerce.repositories.OrderRepository;
import com.hnq.e_commerce.repositories.ProductRepository;
import com.hnq.e_commerce.repositories.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewServiceImpl implements ReviewService {


     ReviewRepository reviewRepository;


     ProductRepository productRepository;

     UserRepository userRepository;
     OrderRepository orderRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewRequest reviewRequest, String email) {
        // Tìm người dùng theo email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng"));

        // Tìm sản phẩm
        Product product = productRepository.findById(reviewRequest.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm"));

        // Kiểm tra xem người dùng đã đánh giá sản phẩm này chưa
        if (reviewRepository.existsByUserIdAndProductId(user.getId(), reviewRequest.getProductId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                              "Bạn đã đánh giá sản phẩm này rồi");
        }

        // Tạo review mới
        Review review = Review.builder()
                .rating(reviewRequest.getRating())
                .comment(reviewRequest.getComment())
                .product(product)
                .user(user)
                .isGuestReview(false)
                .build();

        Review savedReview = reviewRepository.save(review);

        // Cập nhật rating trung bình của sản phẩm
        updateProductRating(product.getId());

        return mapToReviewResponse(savedReview);
    }

    @Override
    public Page<ReviewResponse> getProductReviews(String productId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByProductId(productId, pageable);
        return reviews.map(this::mapToReviewResponse);
    }

    @Override
    public Page<ReviewResponse> getUserReviews(String userId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByUserId(userId, pageable);
        return reviews.map(this::mapToReviewResponse);
    }

    @Override
    public ReviewResponse getReviewById(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đánh giá"));
        return mapToReviewResponse(review);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(String reviewId, ReviewRequest reviewRequest, String userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đánh giá"));

        // Kiểm tra xem người dùng có quyền chỉnh sửa đánh giá không
        if (!review.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền chỉnh sửa đánh giá này");
        }

        review.setRating(reviewRequest.getRating());
        review.setComment(reviewRequest.getComment());

        Review updatedReview = reviewRepository.save(review);

        // Cập nhật rating trung bình của sản phẩm
        updateProductRating(review.getProduct().getId());

        return mapToReviewResponse(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(String reviewId, String userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đánh giá"));

        // Kiểm tra xem người dùng có quyền xóa đánh giá không
        if (!review.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xóa đánh giá này");
        }

        String productId = review.getProduct().getId();

        reviewRepository.delete(review);

        // Cập nhật rating trung bình của sản phẩm
        updateProductRating(productId);
    }

    @Override
    public boolean canUserReviewProduct(String userId, String productId, String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng"));

        // Kiểm tra xem đơn hàng đã giao thành công chưa
        if (!order.getOrderStatus().equals(OrderStatus.DELIVERED)) {
            return false;
        }

        // Kiểm tra xem đơn hàng có thuộc về người dùng không
        if (!order.getUser().getId().equals(userId)) {
            return false;
        }

        // Kiểm tra xem sản phẩm có trong đơn hàng không
        boolean productInOrder = order.getOrderItemList().stream()
                .anyMatch(item -> item.getProduct().getId().equals(productId));

        if (!productInOrder) {
            return false;
        }

        // Kiểm tra xem người dùng đã đánh giá sản phẩm này trong đơn hàng này chưa
        return !reviewRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public Float getAverageRatingForProduct(String productId) {
        return reviewRepository.calculateAverageRatingForProduct(productId).orElse(0.0f);
    }

    @Override
    @Transactional
    public void updateProductRating(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm"));

        // Đếm số lượng đánh giá cho sản phẩm
        long reviewCount = reviewRepository.count(
                (root, query, builder) -> builder.equal(root.get("product").get("id"), productId)
        );

        if (reviewCount > 1) {
            // Nếu có nhiều hơn 1 đánh giá, sử dụng rating trung bình
            Optional<Float> avgRating = reviewRepository.calculateAverageRatingForProduct(productId);
            if (avgRating.isPresent()) {
                product.setRating(avgRating.get());
            } else {
                product.setRating(null);
            }
        } else if (reviewCount == 1) {
            // Nếu chỉ có 1 đánh giá, sử dụng rating của đánh giá đó
            Page<Review> reviewPage = reviewRepository.findByProductId(productId, PageRequest.of(0, 1));
            if (!reviewPage.isEmpty()) {
                Review singleReview = reviewPage.getContent().get(0);
                product.setRating(singleReview.getRating().floatValue());
            }
        } else {
            // Nếu không có đánh giá nào
            product.setRating(null);
        }

        // Lưu lại sản phẩm với rating mới
        productRepository.save(product);
    }

    @Override
    public RatingStatisticsResponse getProductRatingStatistics(String productId) {
        // Kiểm tra sản phẩm tồn tại
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm"));

        // Lấy tổng số review
        long totalReviews = reviewRepository.countByProductId(productId);

        if (totalReviews == 0) {
            return RatingStatisticsResponse.builder()
                    .productId(productId)
                    .averageRating(0)
                    .totalReviews(0)
                    .ratingDistribution(new RatingStatisticsResponse.RatingDistribution())
                    .build();
        }

        // Đếm số lượng mỗi loại rating
        long fiveStarCount = reviewRepository.countByProductIdAndRating(productId, 5);
        long fourStarCount = reviewRepository.countByProductIdAndRating(productId, 4);
        long threeStarCount = reviewRepository.countByProductIdAndRating(productId, 3);
        long twoStarCount = reviewRepository.countByProductIdAndRating(productId, 2);
        long oneStarCount = reviewRepository.countByProductIdAndRating(productId, 1);

        // Tính phần trăm
        double fiveStarPercent = (double) fiveStarCount / totalReviews * 100;
        double fourStarPercent = (double) fourStarCount / totalReviews * 100;
        double threeStarPercent = (double) threeStarCount / totalReviews * 100;
        double twoStarPercent = (double) twoStarCount / totalReviews * 100;
        double oneStarPercent = (double) oneStarCount / totalReviews * 100;

        // Tạo đối tượng phân phối rating
        RatingStatisticsResponse.RatingDistribution distribution = RatingStatisticsResponse.RatingDistribution.builder()
                .fiveStarCount(fiveStarCount)
                .fourStarCount(fourStarCount)
                .threeStarCount(threeStarCount)
                .twoStarCount(twoStarCount)
                .oneStarCount(oneStarCount)
                .fiveStarPercent(Math.round(fiveStarPercent * 100.0) / 100.0)
                .fourStarPercent(Math.round(fourStarPercent * 100.0) / 100.0)
                .threeStarPercent(Math.round(threeStarPercent * 100.0) / 100.0)
                .twoStarPercent(Math.round(twoStarPercent * 100.0) / 100.0)
                .oneStarPercent(Math.round(oneStarPercent * 100.0) / 100.0)
                .build();

        // Lấy rating trung bình
        float averageRating = getAverageRatingForProduct(productId);

        return RatingStatisticsResponse.builder()
                .productId(productId)
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .ratingDistribution(distribution)
                .build();
    }


    private ReviewResponse mapToReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .userId(review.getUser().getId())
                .username(review.getUser().getEmail())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
