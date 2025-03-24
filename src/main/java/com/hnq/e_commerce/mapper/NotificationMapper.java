package com.hnq.e_commerce.mapper;

import com.hnq.e_commerce.dto.NotificationDTO;
import com.hnq.e_commerce.entities.Notification;
import org.springframework.stereotype.Component;



@Component
public class NotificationMapper {

    public NotificationDTO toDTO(Notification notification) {
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
                .userId(notification.getUser().getId())
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

