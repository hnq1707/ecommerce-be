package com.hnq.e_commerce.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.hnq.e_commerce.auth.entities.Role;
import com.hnq.e_commerce.auth.entities.User;


import com.hnq.e_commerce.auth.repositories.UserRepository;
import com.hnq.e_commerce.dto.NotificationDTO;
import com.hnq.e_commerce.entities.Notification;
import com.hnq.e_commerce.entities.NotificationType;
import com.hnq.e_commerce.mapper.NotificationMapper;
import com.hnq.e_commerce.repositories.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationMapper notificationMapper;

    // Phương thức cho người dùng thông thường
    public List<NotificationDTO> getUserNotifications(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        List<Notification> notifications = notificationRepository.findByUserAndDeletedFalseOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getUserUnreadNotifications(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        List<Notification> notifications =
                notificationRepository.findByUserAndIsReadFalseAndDeletedFalseOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return notificationRepository.countByUserAndIsReadFalseAndDeletedFalse(user);
    }
    public List<NotificationDTO> getUserReadNotifications(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Sử dụng Pageable.unpaged() để lấy tất cả không theo trang
        List<Notification> notifications = notificationRepository
                .findByUserAndIsReadAndDeletedFalseOrderByCreatedAtDesc(user, true, Pageable.unpaged())
                .getContent();
        return notifications.stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả thông báo đã lưu trữ của người dùng
     */
    public List<NotificationDTO> getUserArchivedNotifications(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        List<Notification> notifications = notificationRepository
                .findByUserAndArchivedTrueAndDeletedFalseOrderByCreatedAtDesc(user, Pageable.unpaged())
                .getContent();
        return notifications.stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả thông báo đã xóa mềm (deleted = true) của người dùng
     */
    public List<NotificationDTO> getUserDeletedNotifications(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        List<Notification> notifications = notificationRepository
                .findByUserAndDeletedTrueOrderByDeletedAtDesc(user, Pageable.unpaged())
                .getContent();
        return notifications.stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }
    @Transactional
    public NotificationDTO createNotification(String userId, String title, String message,
                                              NotificationType type, String link,
                                              String senderId, String senderName, String senderAvatar) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Notification notification = Notification.builder()
                .user(user)
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
        NotificationDTO notificationDTO = notificationMapper.toDTO(savedNotification);

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
        return notificationMapper.toDTO(updatedNotification);
    }

    @Transactional
    public void markAllAsRead(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        notificationRepository.markAllAsRead(user);
    }

    @Transactional
    public void deleteNotification(String notificationId) {
        notificationRepository.softDeleteById(notificationId);
    }

    @Transactional
    public void deleteAllUserNotifications(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        notificationRepository.softDeleteAllByUser(user);
    }

    // Send a system notification to a specific user
    @Transactional
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

    // Phương thức cho admin
    /**
     * Lấy tất cả thông báo dành cho admin
     */
    public List<NotificationDTO> getAdminNotifications() {
        List<Notification> notifications = notificationRepository.findAdminNotifications();
        return notifications.stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông báo chưa đọc dành cho admin
     */
    public List<NotificationDTO> getAdminUnreadNotifications() {
        List<Notification> notifications = notificationRepository.findAdminUnreadNotifications();
        return notifications.stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Đếm số lượng thông báo chưa đọc dành cho admin
     */
    public long getAdminUnreadCount() {
        return notificationRepository.countAdminUnreadNotifications();
    }

    /**
     * Đánh dấu tất cả thông báo admin là đã đọc
     */
    @Transactional
    public void markAllAdminNotificationsAsRead() {
        notificationRepository.markAllAdminNotificationsAsRead();
    }

    /**
     * Xóa tất cả thông báo admin
     */
    @Transactional
    public void clearAllAdminNotifications() {
        notificationRepository.deleteAllAdminNotifications();
    }

    /**
     * Lưu trữ tất cả thông báo đã đọc của admin
     */
    @Transactional
    public void archiveAllReadAdminNotifications() {
        notificationRepository.archiveAllReadAdminNotifications();
    }

    /**
     * Gửi thông báo cho tất cả admin
     */
    public void sendAdminBroadcastNotification(String title, String message, String link, NotificationType type) {
        // Tạo DTO thông báo
        NotificationDTO broadcastNotification = NotificationDTO.builder()
                .title(title)
                .message(message)
                .type(type != null ? type : NotificationType.SYSTEM)
                .isRead(false)
                .link(link)
                .createdAt(LocalDateTime.now())
                .build();

        // Gửi thông báo qua WebSocket
        messagingTemplate.convertAndSend("/topic/admin-notifications", broadcastNotification);

        // Lưu thông báo vào database cho tất cả admin
        List<User> adminUsers = userRepository.findByRolesName("ADMIN");
        for (User admin : adminUsers) {
            Notification notification = Notification.builder()
                    .user(admin)
                    .title(title)
                    .message(message)
                    .type(type != null ? type : NotificationType.SYSTEM)
                    .isRead(false)
                    .link(link)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
        }
    }

    /**
     * Gửi thông báo cho tất cả người dùng có vai trò cụ thể
     */
    public void notifyUsersByRole(String roleName, String title, String message, String link, NotificationType type) {
        List<User> users = userRepository.findByRolesName(roleName);

        for (User user : users) {
            Notification notification = Notification.builder()
                    .user(user)
                    .title(title)
                    .message(message)
                    .type(type)
                    .isRead(false)
                    .link(link)
                    .createdAt(LocalDateTime.now())
                    .build();

            Notification savedNotification = notificationRepository.save(notification);
            NotificationDTO notificationDTO = notificationMapper.toDTO(savedNotification);

            // Gửi thông báo real-time qua WebSocket
            messagingTemplate.convertAndSendToUser(user.getId(), "/queue/notifications", notificationDTO);
        }
    }

    /**
     * Gửi thông báo khi có đơn hàng mới cho tất cả admin
     */
    public void notifyNewOrder(String orderNumber, String orderId, String customerId, String customerName, double totalAmount) {
        String title = "Đơn hàng mới #" + orderNumber;
        String message = "Đơn hàng mới #" + orderNumber + " đã được tạo bởi khách hàng " +
                customerName + " với tổng giá trị " +
                formatCurrency(totalAmount) + ".";
        String link = "/admin/orders/" + orderId;

        notifyUsersByRole("ADMIN", title, message, link, NotificationType.ORDER);
    }

    /**
     * Gửi thông báo khi đơn hàng bị hủy cho tất cả admin
     */
    public void notifyCancelledOrder(String orderNumber, String orderId, String customerId, String customerName, String cancelReason) {
        String title = "Đơn hàng bị hủy #" + orderNumber;
        String message = "Đơn hàng #" + orderNumber + " của khách hàng " + customerName +
                " đã bị hủy. Lý do: " + (cancelReason != null ? cancelReason : "Không có lý do");
        String link = "/admin/orders/" + orderId;

        notifyUsersByRole("ADMIN", title, message, link, NotificationType.ORDER);
    }

    /**
     * Format số tiền thành định dạng tiền tệ
     */
    private String formatCurrency(double amount) {
        return String.format("%,.0f VNĐ", amount);
    }
}

