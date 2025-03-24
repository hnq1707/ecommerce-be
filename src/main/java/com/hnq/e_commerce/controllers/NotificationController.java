package com.hnq.e_commerce.controllers;

import java.util.List;


import com.hnq.e_commerce.dto.NotificationDTO;
import com.hnq.e_commerce.entities.NotificationType;
import com.hnq.e_commerce.services.NotificationService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Endpoints cho người dùng thông thường
    @GetMapping("/notifications/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @GetMapping("/notifications/user/{userId}/unread")
    public ResponseEntity<List<NotificationDTO>> getUserUnreadNotifications(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getUserUnreadNotifications(userId));
    }

    @GetMapping("/notifications/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }
    @GetMapping("/notifications/user/{userId}/read")
    public ResponseEntity<List<NotificationDTO>> getUserReadNotifications(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getUserReadNotifications(userId));
    }

    /**
     * Lấy thông báo đã lưu trữ của người dùng
     */
    @GetMapping("/notifications/user/{userId}/archived")
    public ResponseEntity<List<NotificationDTO>> getUserArchivedNotifications(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getUserArchivedNotifications(userId));
    }

    /**
     * Lấy thông báo đã xóa mềm của người dùng
     */
    @GetMapping("/notifications/user/{userId}/deleted")
    public ResponseEntity<List<NotificationDTO>> getUserDeletedNotifications(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getUserDeletedNotifications(userId));
    }

    @PostMapping("/notifications/user/{userId}")
    public ResponseEntity<NotificationDTO> createNotification(
            @PathVariable String userId,
            @RequestBody CreateNotificationRequest request) {

        NotificationDTO notification = notificationService.createNotification(
                userId,
                request.getTitle(),
                request.getMessage(),
                request.getType(),
                request.getLink(),
                request.getSenderId(),
                request.getSenderName(),
                request.getSenderAvatar()
        );

        return ResponseEntity.ok(notification);
    }

    @PutMapping("/notifications/{notificationId}/read")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable String notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }

    @PutMapping("/notifications/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable String userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/notifications/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable String notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/notifications/user/{userId}")
    public ResponseEntity<Void> deleteAllUserNotifications(@PathVariable String userId) {
        notificationService.deleteAllUserNotifications(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/notifications/broadcast")
    public ResponseEntity<Void> sendBroadcastNotification(@RequestBody BroadcastNotificationRequest request) {
        notificationService.sendBroadcastNotification(
                request.getTitle(),
                request.getMessage(),
                request.getLink()
        );
        return ResponseEntity.ok().build();
    }

    // Endpoints cho admin
    @GetMapping("/admin/notifications")
    public ResponseEntity<List<NotificationDTO>> getAdminNotifications() {
        return ResponseEntity.ok(notificationService.getAdminNotifications());
    }

    @GetMapping("/admin/notifications/unread")
    public ResponseEntity<List<NotificationDTO>> getAdminUnreadNotifications() {
        return ResponseEntity.ok(notificationService.getAdminUnreadNotifications());
    }

    @GetMapping("/admin/notifications/unread-count")
    public ResponseEntity<Long> getAdminUnreadCount() {
        return ResponseEntity.ok(notificationService.getAdminUnreadCount());
    }

    @PutMapping("/admin/notifications/read-all")
    public ResponseEntity<Void> markAllAdminNotificationsAsRead() {
        notificationService.markAllAdminNotificationsAsRead();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/admin/notifications/clear-all")
    public ResponseEntity<Void> clearAllAdminNotifications() {
        notificationService.clearAllAdminNotifications();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/notifications/broadcast")
    public ResponseEntity<Void> sendAdminBroadcastNotification(@RequestBody AdminBroadcastNotificationRequest request) {
        notificationService.sendAdminBroadcastNotification(
                request.getTitle(),
                request.getMessage(),
                request.getLink(),
                request.getType()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/notifications/archive-all-read")
    public ResponseEntity<Void> archiveAllReadAdminNotifications() {
        notificationService.archiveAllReadAdminNotifications();
        return ResponseEntity.ok().build();
    }

    // Request DTOs
    @Setter
    @Getter
    public static class CreateNotificationRequest {
        // Getters and setters
        private String title;
        private String message;
        private NotificationType type;
        private String link;
        private String senderId;
        private String senderName;
        private String senderAvatar;

    }

    @Setter
    @Getter
    public static class BroadcastNotificationRequest {
        // Getters and setters
        private String title;
        private String message;
        private String link;

    }

    @Setter
    @Getter
    public static class AdminBroadcastNotificationRequest {
        // Getters and setters
        private String title;
        private String message;
        private String link;
        private NotificationType type;

    }
}

