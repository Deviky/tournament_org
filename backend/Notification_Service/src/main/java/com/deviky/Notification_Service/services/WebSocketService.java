package com.deviky.Notification_Service.services;

import com.deviky.Notification_Service.dto.NotificationWebSocket;
import com.deviky.Notification_Service.models.NotificationEntity;
import com.deviky.Notification_Service.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate template;

    public void sendToUser(Long userId, String message) throws Exception {
        try {
            template.convertAndSendToUser(userId.toString(), "/queue/notifications", message);
        } catch (Exception e) {
            throw new Exception("Ошибка отправки WS пользователю " + userId + ": " + e.getMessage());
        }
    }

}