package com.hnq.e_commerce.controllers;





import java.util.List;


import com.hnq.e_commerce.dto.NotificationDTO;
import com.hnq.e_commerce.entities.NotificationType;
import com.hnq.e_commerce.services.NotificationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationDTO>> getUserUnreadNotifications(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getUserUnreadNotifications(userId));
    }
    @GetMapping("/user/{userId}/read")
    public ResponseEntity<List<NotificationDTO>> getUserReadNotifications(
            @PathVariable String userId)
          {
        return ResponseEntity.ok(notificationService.getUserReadNotifications(userId));
    }


    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PostMapping("/user/{userId}")
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

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable String notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable String userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable String notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteAllUserNotifications(@PathVariable String userId) {
        notificationService.deleteAllUserNotifications(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/broadcast")
    public ResponseEntity<Void> sendBroadcastNotification(@RequestBody BroadcastNotificationRequest request) {
        notificationService.sendBroadcastNotification(
                request.getTitle(),
                request.getMessage(),
                request.getLink()
        );
        return ResponseEntity.ok().build();
    }

    // Request DTOs
    @Data
    public static class CreateNotificationRequest {
        private String title;
        private String message;
        private NotificationType type;
        private String link;
        private String senderId;
        private String senderName;
        private String senderAvatar;


    }

    @Data
    public static class BroadcastNotificationRequest {
        private String title;
        private String message;
        private String link;


    }
}


