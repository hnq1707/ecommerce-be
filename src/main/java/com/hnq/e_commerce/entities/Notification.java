package com.hnq.e_commerce.entities;

import java.time.LocalDateTime;

import com.hnq.e_commerce.auth.entities.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notification_user_id", columnList = "user_id"),
                @Index(name = "idx_notification_created_at", columnList = "createdAt"),
                @Index(name = "idx_notification_read", columnList = "is_read")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private boolean isRead;

    private String link;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(name = "sender_id")
    private String senderId;

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "sender_avatar")
    private String senderAvatar;

    // Thêm trường để theo dõi trạng thái lưu trữ
    @Column(nullable = false)
    private boolean archived = false;

    // Thêm trường để theo dõi trạng thái xóa mềm
    @Column(nullable = false)
    private boolean deleted = false;

    // Thời gian khi thông báo bị xóa mềm
    private LocalDateTime deletedAt;

    // Thời gian khi thông báo được lưu trữ
    private LocalDateTime archivedAt;

    // Trường để lưu trữ dữ liệu bổ sung dưới dạng JSON
    @Column(columnDefinition = "TEXT")
    private String additionalData;

    // Pre-persist hook để tự động thiết lập createdAt
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Pre-update hook để tự động thiết lập updatedAt
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Phương thức để đánh dấu thông báo là đã lưu trữ
    public void archive() {
        this.archived = true;
        this.archivedAt = LocalDateTime.now();
    }

    // Phương thức để đánh dấu thông báo là đã xóa (xóa mềm)
    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    // Phương thức để khôi phục thông báo đã xóa mềm
    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
    }
}

