package com.hnq.e_commerce.repositories;

import java.util.List;

import com.hnq.e_commerce.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    // Sửa tên method: thay "AndRead" thành "AndIsRead"
    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(String userId, boolean isRead);

    // Đếm số thông báo chưa đọc cho một user
    long countByUserIdAndIsRead(String userId, boolean isRead);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId")
    void markAllAsIsRead(@Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.userId = :userId")
    void deleteAllByUserId(@Param("userId") String userId);
}
