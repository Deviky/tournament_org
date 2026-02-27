package com.deviky.Notification_Service.components;

import com.deviky.Notification_Service.dto.Notification;
import com.deviky.Notification_Service.models.NotificationEmailStatus;
import com.deviky.Notification_Service.models.NotificationEntity;
import com.deviky.Notification_Service.models.NotificationWebSocketStatus;
import com.deviky.Notification_Service.repositories.NotificationRepository;
import com.deviky.Notification_Service.services.EmailService;
import com.deviky.Notification_Service.services.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final WebSocketService webSocketService;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @KafkaListener(topics = "notifications", groupId = "notification-service")
    public void listen(Notification notification,
                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                       @Header(KafkaHeaders.OFFSET) long offset) {

        NotificationEntity entity = new NotificationEntity();
        entity.setReceiverUserId(notification.getReceiverUserId());
        entity.setWebSocketMessage(notification.getWebSocketMessage());
        entity.setEmailMessage(notification.getEmailMessage());

        entity.setWebSocketStatus(NotificationWebSocketStatus.UNREAD);
        entity.setEmailStatus(NotificationEmailStatus.PENDING);

        entity.setKafkaTopic(topic);
        entity.setKafkaPartition(partition);
        entity.setKafkaOffset(offset);

        notificationRepository.save(entity);

        String userEmail = "Dennis.michurin@gmail.com"; //ЗАГЛУШКА

        // Отправка WebSocket, если онлайн
        if (entity.getWebSocketMessage() != null) {
            try {
                webSocketService.sendToUser(entity.getReceiverUserId(), entity.getWebSocketMessage());
            } catch (Exception e) {
                entity.setLastErrorWebSocket(e.getMessage());
            }
        }

        // Отправка Email
        if (entity.getEmailMessage() != null) {
            try {
                emailService.sendEmail(userEmail, "Уведомление", entity.getEmailMessage());
                entity.setEmailStatus(NotificationEmailStatus.SENT);
            } catch (Exception e) {
                entity.setEmailStatus(NotificationEmailStatus.FAILED);
                entity.setLastErrorEmail(e.getMessage());
            }
        }

        notificationRepository.save(entity);
    }
}
