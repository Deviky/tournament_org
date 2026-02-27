package com.deviky.Notification_Service.repositories;

import com.deviky.Notification_Service.models.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    @Query(value = "SELECT * FROM notification.notification_logs " +
            "WHERE receiver_user_id = :userId " +
            "AND web_socket_message IS NOT NULL " +
            "AND web_socket_message <> '' " +
            "ORDER BY created_at DESC " +
            "LIMIT 100", nativeQuery = true)
    List<NotificationEntity> findLast100WebSocketNotifications(@Param("userId") Long userId);
}
