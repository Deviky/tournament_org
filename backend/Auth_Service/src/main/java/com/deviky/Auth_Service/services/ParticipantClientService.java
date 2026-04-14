package com.deviky.Auth_Service.services;

import com.deviky.Auth_Service.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipantClientService {
    private final WebClient.Builder webClientBuilder;

    private WebClient getWebClient() {
        return webClientBuilder.baseUrl("http://PARTICIPANT-SERVICE").build();
    }

    public ApiResponse<Player> createPlayerProfile(CreatePlayerRequest dto) {
        try {
            return getWebClient().post()
                    .uri("/api/participant/players/private/create")
                    .bodyValue(dto)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            response -> response.bodyToMono(String.class)
                                    .flatMap(msg -> {
                                        try {
                                            ObjectMapper mapper = new ObjectMapper();
                                            ApiResponse<?> apiResponse = mapper.readValue(msg, new TypeReference<ApiResponse<?>>() {});
                                            return Mono.error(new RuntimeException(apiResponse.getMessage()));
                                        } catch (Exception e) {
                                            // если JSON не парсится — оставляем сырое сообщение
                                            return Mono.error(new RuntimeException(msg));
                                        }
                                    }))
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Player>>() {})
                    .block();
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<Organization> createOrganizationProfile(CreateOrganizationRequest dto) {
        try {
            return getWebClient().post()
                    .uri("/api/participant/organizations/private/create")
                    .bodyValue(dto)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            response -> response.bodyToMono(String.class)
                                    .map(msg -> new RuntimeException("Ошибка создания организации: " + msg)))
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Organization>>() {})
                    .block();
        } catch (Exception e) {
            return new ApiResponse<>("Ошибка сервера при создании организации: " + e.getMessage(), null, true);
        }
    }
}

