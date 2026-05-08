package com.deviky.Tournament_Service.tournament_core.services;

import com.deviky.Tournament_Service.tournament_core.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipantClientService {
    private final WebClient.Builder webClientBuilder;
    private WebClient getWebClient() {
        return webClientBuilder.baseUrl("http://PARTICIPANT-SERVICE").build();
    }

    public ApiResponse<Team> getTeam(Long teamId) {
        try {
            return getWebClient().get()
                    .uri("api/participant/teams/public/get/" + teamId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            response -> response.bodyToMono(String.class)
                                    .map(msg -> new RuntimeException(msg)))
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Team>>() {})
                    .block();
        } catch (Exception e) {
            return new ApiResponse<>("Ошибка сервера: " + e.getMessage(), null, true);
        }
    }

    public ApiResponse<List<Team>> getTeams(List<Long> teamIds) {
        try {
            return getWebClient().get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/participant/teams/public/get_by_ids")
                            .queryParam("teamIds", teamIds)
                            .build()
                    )
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<Team>>>() {})
                    .block();

        } catch (Exception e) {
            return new ApiResponse<>("Ошибка получения команд: " + e.getMessage(), null, true);
        }
    }

    public ApiResponse<Organization> getOrganization(Long organizationId) {
        try {
            return getWebClient().get()
                    .uri("/api/participant/organizations/public/get/" + organizationId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Organization>>() {})
                    .block();
        } catch (Exception e) {
            return new ApiResponse<>("Ошибка получения организации: " + e.getMessage(), null, true);
        }
    }
}
