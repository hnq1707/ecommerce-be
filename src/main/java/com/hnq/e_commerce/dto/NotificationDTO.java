package com.hnq.e_commerce.dto;

import java.time.LocalDateTime;


import com.hnq.e_commerce.entities.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private String id;
    private String userId;
    private String title;
    private String message;
    private NotificationType type;
    private boolean isRead;
    private String link;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private SenderDTO sender;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SenderDTO {
        private String id;
        private String name;
        private String avatar;
    }
}

