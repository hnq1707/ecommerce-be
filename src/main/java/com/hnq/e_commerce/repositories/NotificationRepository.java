package com.hnq.e_commerce.repositories;

import java.time.LocalDateTime;
import java.util.List;

import com.hnq.e_commerce.auth.entities.User;
import com.hnq.e_commerce.entities.Notification;
import com.hnq.e_commerce.entities.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    // Tìm thông báo theo user, sắp xếp theo thời gian tạo giảm dần
    List<Notification> findByUserAndDeletedFalseOrderByCreatedAtDesc(User user);

    // Tìm thông báo theo user và phân trang
    Page<Notification> findByUserAndDeletedFalseOrderByCreatedAtDesc(User user, Pageable pageable);

    // Tìm thông báo theo user và trạng thái chưa đọc
    List<Notification> findByUserAndIsReadFalseAndDeletedFalseOrderByCreatedAtDesc(User user);

    // Tìm thông báo theo user và trạng thái đã đọc
    Page<Notification> findByUserAndIsReadAndDeletedFalseOrderByCreatedAtDesc(User user, boolean isRead, Pageable pageable);

    // Tìm thông báo theo user và loại thông báo
    Page<Notification> findByUserAndTypeAndDeletedFalseOrderByCreatedAtDesc(User user, NotificationType type, Pageable pageable);

    // Tìm thông báo đã lưu trữ
    Page<Notification> findByUserAndArchivedTrueAndDeletedFalseOrderByCreatedAtDesc(User user, Pageable pageable);

    // Tìm thông báo đã xóa mềm (thùng rác)
    Page<Notification> findByUserAndDeletedTrueOrderByDeletedAtDesc(User user, Pageable pageable);

    // Đếm số lượng thông báo chưa đọc
    long countByUserAndIsReadFalseAndDeletedFalse(User user);

    // Tìm kiếm thông báo theo nội dung
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.deleted = false " +
            "AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(n.message) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> searchNotifications(
            @Param("user") User user,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    // Đánh dấu tất cả thông báo là đã đọc
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE n.user = :user AND n.isRead = false AND n.deleted = false")
    void markAllAsRead(@Param("user") User user);

    // Lưu trữ tất cả thông báo đã đọc
    @Modifying
    @Query("UPDATE Notification n SET n.archived = true, n.archivedAt = CURRENT_TIMESTAMP " +
            "WHERE n.user = :user AND n.isRead = true AND n.archived = false AND n.deleted = false")
    void archiveAllReadNotifications(@Param("user") User user);

    // Xóa mềm thông báo theo ID
    @Modifying
    @Query("UPDATE Notification n SET n.deleted = true, n.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE n.id = :notificationId")
    void softDeleteById(@Param("notificationId") String notificationId);

    // Xóa mềm tất cả thông báo của người dùng
    @Modifying
    @Query("UPDATE Notification n SET n.deleted = true, n.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE n.user = :user AND n.deleted = false")
    void softDeleteAllByUser(@Param("user") User user);

    // Khôi phục thông báo đã xóa mềm theo ID
    @Modifying
    @Query("UPDATE Notification n SET n.deleted = false, n.deletedAt = null " +
            "WHERE n.id = :notificationId")
    void restoreById(@Param("notificationId") String notificationId);

    // Xóa vĩnh viễn thông báo đã xóa mềm quá 30 ngày
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.deleted = true AND n.deletedAt < :date")
    void permanentlyDeleteOldNotifications(@Param("date") LocalDateTime date);

    // Lấy thông báo theo khoảng thời gian
    Page<Notification> findByUserAndDeletedFalseAndCreatedAtBetweenOrderByCreatedAtDesc(
            User user, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Các phương thức cho Admin Notifications

    // Lấy tất cả thông báo dành cho admin
    @Query("SELECT n FROM Notification n JOIN n.user u JOIN u.roles r " +
            "WHERE r.name = 'ADMIN' AND n.deleted = false " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findAdminNotifications();

    // Lấy thông báo chưa đọc dành cho admin
    @Query("SELECT n FROM Notification n JOIN n.user u JOIN u.roles r " +
            "WHERE r.name = 'ADMIN' AND n.isRead = false AND n.deleted = false " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findAdminUnreadNotifications();

    // Đếm số lượng thông báo chưa đọc dành cho admin
    @Query("SELECT COUNT(n) FROM Notification n JOIN n.user u JOIN u.roles r " +
            "WHERE r.name = 'ADMIN' AND n.isRead = false AND n.deleted = false")
    long countAdminUnreadNotifications();

    // Đánh dấu tất cả thông báo admin là đã đọc
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE n.user IN (SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ADMIN') " +
            "AND n.isRead = false AND n.deleted = false")
    void markAllAdminNotificationsAsRead();

    // Xóa tất cả thông báo admin
    @Modifying
    @Query("UPDATE Notification n SET n.deleted = true, n.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE n.user IN (SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ADMIN') " +
            "AND n.deleted = false")
    void deleteAllAdminNotifications();

    // Lưu trữ tất cả thông báo đã đọc của admin
    @Modifying
    @Query("UPDATE Notification n SET n.archived = true, n.archivedAt = CURRENT_TIMESTAMP " +
            "WHERE n.user IN (SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ADMIN') " +
            "AND n.isRead = true AND n.archived = false AND n.deleted = false")
    void archiveAllReadAdminNotifications();
}
