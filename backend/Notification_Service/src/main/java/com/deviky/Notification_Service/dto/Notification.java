package com.deviky.Notification_Service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    Long receiverUserId;
    String emailMessage;
    String webSocketMessage;
}
