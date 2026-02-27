package com.deviky.Notification_Service.dto;

import com.deviky.Notification_Service.models.NotificationWebSocketStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationWebSocket {
    Long id;
    Long receiverUserId;
    String webSocketMessage;
    NotificationWebSocketStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt;
}
