package com.deviky.Notification_Service.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Настройка брокера сообщений
        config.enableSimpleBroker("/topic", "/queue"); // топики для broadcast и личных сообщений
        config.setApplicationDestinationPrefixes("/app"); // префикс для сообщений от клиента к серверу
        config.setUserDestinationPrefix("/user");         // префикс для личных сообщений
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint, куда подключается клиент
        registry.addEndpoint("/ws")               // вот тут указываем "ws" в начале
                .setAllowedOriginPatterns("*")   // разрешаем CORS
                .withSockJS();                   // fallback через SockJS
    }
}