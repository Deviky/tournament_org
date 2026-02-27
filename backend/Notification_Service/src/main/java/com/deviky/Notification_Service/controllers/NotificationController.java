package com.deviky.Notification_Service.controllers;

import com.deviky.Notification_Service.dto.ApiResponse;
import com.deviky.Notification_Service.dto.NotificationWebSocket;
import com.deviky.Notification_Service.models.NotificationEntity;
import com.deviky.Notification_Service.models.NotificationWebSocketStatus;
import com.deviky.Notification_Service.repositories.NotificationRepository;
import com.deviky.Notification_Service.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/notification")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/get-notifications")
    public ResponseEntity<ApiResponse<List<NotificationWebSocket>>> setNotificationsRead(
            @RequestHeader("X-Player-Id") Long selfId
    ) {
        ApiResponse<List<NotificationWebSocket>> response = notificationService.getOldNotifications(selfId);
        HttpStatus status = response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/set-notifications-read")
    public ResponseEntity<ApiResponse<Void>> setNotificationsRead(
            @RequestBody List<Long> notificationIds
    ) {
        ApiResponse<Void> response = notificationService.setNotificationsRead(notificationIds);
        HttpStatus status = response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }
}
