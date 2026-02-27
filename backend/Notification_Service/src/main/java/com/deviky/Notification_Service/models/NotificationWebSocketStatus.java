package com.deviky.Notification_Service.models;

public enum NotificationWebSocketStatus {
    UNREAD,     // ещё не отправлено на клиент (пользователь оффлайн)
    READ        // пользователь увидел/подтвердил уведомление (например, кликнул "звоночок")
}
