package com.hnq.e_commerce.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


import com.hnq.e_commerce.dto.NotificationDTO;
import com.hnq.e_commerce.entities.Notification;
import com.hnq.e_commerce.entities.NotificationType;
import com.hnq.e_commerce.repositories.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public List<NotificationDTO> getUserNotifications(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getUserUnreadNotifications(String userId) {
        List<Notification> notifications =
                notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    public List<NotificationDTO> getUserReadNotifications(String userId) {
        List<Notification> notifications =
                notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, true);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Transactional
    public NotificationDTO createNotification(String userId, String title, String message,
                                              NotificationType type, String link,
                                              String senderId, String senderName, String senderAvatar) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .link(link)
                .senderId(senderId)
                .senderName(senderName)
                .senderAvatar(senderAvatar)
                .createdAt(LocalDateTime.now())
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        NotificationDTO notificationDTO = convertToDTO(savedNotification);

        // Send real-time notification via WebSocket
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notificationDTO);

        return notificationDTO;
    }

    @Transactional
    public NotificationDTO markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        notification.setUpdatedAt(LocalDateTime.now());

        Notification updatedNotification = notificationRepository.save(notification);
        return convertToDTO(updatedNotification);
    }

    @Transactional
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsIsRead(userId);
    }

    @Transactional
    public void deleteNotification(String notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Transactional
    public void deleteAllUserNotifications(String userId) {
        notificationRepository.deleteAllByUserId(userId);
    }

    // Send a system notification to a specific user
    public NotificationDTO sendSystemNotification(String userId, String title, String message, String link) {
        return createNotification(userId, title, message, NotificationType.SYSTEM, link, null, null, null);
    }

    // Send a broadcast notification to all users (via topic)
    public void sendBroadcastNotification(String title, String message, String link) {
        NotificationDTO broadcastNotification = NotificationDTO.builder()
                .title(title)
                .message(message)
                .type(NotificationType.SYSTEM)
                .isRead(false)
                .link(link)
                .createdAt(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/notifications", broadcastNotification);
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO.SenderDTO sender = null;

        if (notification.getSenderId() != null) {
            sender = NotificationDTO.SenderDTO.builder()
                    .id(notification.getSenderId())
                    .name(notification.getSenderName())
                    .avatar(notification.getSenderAvatar())
                    .build();
        }

        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.isRead())
                .link(notification.getLink())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .sender(sender)
                .build();
    }
}

