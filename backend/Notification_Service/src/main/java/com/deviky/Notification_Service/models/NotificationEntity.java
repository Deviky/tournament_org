package com.deviky.Notification_Service.models;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(schema = "notification", name="notification_logs")
@Data
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Nonnull
    @Column(name = "receiver_user_id", nullable = false)
    private Long receiverUserId;

    @Column(name = "email_message")
    private String emailMessage;

    @Column(name = "web_socket_message")
    private String webSocketMessage;

    // Статус уведомления по WebSocket
    @Enumerated(EnumType.STRING)
    @Column(name = "status_websocket")
    private NotificationWebSocketStatus webSocketStatus;

    // Статус уведомления по Email
    @Enumerated(EnumType.STRING)
    @Column(name = "status_email")
    private NotificationEmailStatus emailStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    // Kafka для анализа
    @Column(name = "kafka_topic")
    private String kafkaTopic;

    @Column(name = "kafka_partition")
    private Integer kafkaPartition;

    @Column(name = "kafka_offset")
    private Long kafkaOffset;

    // Ошибкидля WebSocket
    @Column(name = "last_error_websocket")
    private String lastErrorWebSocket;

    // Ошибки для Email
    @Column(name = "last_error_email")
    private String lastErrorEmail;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        lastUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdatedAt = LocalDateTime.now();
    }
}

