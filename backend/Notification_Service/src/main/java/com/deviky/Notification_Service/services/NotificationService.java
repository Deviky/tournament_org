package com.deviky.Notification_Service.services;

import com.deviky.Notification_Service.dto.ApiResponse;
import com.deviky.Notification_Service.dto.NotificationWebSocket;
import com.deviky.Notification_Service.models.NotificationEntity;
import com.deviky.Notification_Service.models.NotificationWebSocketStatus;
import com.deviky.Notification_Service.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public ApiResponse<List<NotificationWebSocket>> getOldNotifications(Long userId){
        try {
            return new ApiResponse<>("Получен список предыдущих уведомлений",
            notificationRepository.findLast100WebSocketNotifications(userId)
                    .stream()
                    .map(this::mapNotificationToWebSocketDto)
                    .toList(),
                    false);
        }
        catch (Exception e){
            return new ApiResponse<>("Ошибка при получении предыдущих уведомлений: " + e, null, true);
        }
    }

    public ApiResponse<Void> setNotificationsRead(List<Long> notificationIds){
        if (notificationIds == null || notificationIds.isEmpty()) {
            return new ApiResponse<>("У вас нет уведомлений", null, false);
        }

        try {

            List<NotificationEntity> notifications = notificationRepository.findAllById(notificationIds);

            for (NotificationEntity notification : notifications) {
                notification.setWebSocketStatus(NotificationWebSocketStatus.READ);
            }

            notificationRepository.saveAll(notifications);
            return new ApiResponse<>("Уведомления прочитаны", null, false);
        }
        catch (Exception e){
            return new ApiResponse<>("При чтении уведомлений возникла ошибка: " + e, null, true);
        }
    }

    private NotificationWebSocket mapNotificationToWebSocketDto(NotificationEntity notificationEntity) {
        return new NotificationWebSocket(
                notificationEntity.getId(),
                notificationEntity.getReceiverUserId(),
                notificationEntity.getWebSocketMessage(),
                notificationEntity.getWebSocketStatus(),
                notificationEntity.getCreatedAt()
        );
    }
}
