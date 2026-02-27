package com.deviky.Tournament_Service.tournament_core.services;

import com.deviky.Tournament_Service.tournament_core.dto.ApiResponse;
import com.deviky.Tournament_Service.tournament_core.dto.Game;
import com.deviky.Tournament_Service.tournament_core.dto.Team;
import com.deviky.Tournament_Service.tournament_core.dto.TournamentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class ParticipantClientService {
    private final WebClient.Builder webClientBuilder;
    private WebClient getWebClient() {
        return webClientBuilder.baseUrl("http://localhost:8070").build();
    }

    public ApiResponse<Team> getTeam(Long teamId) {
        try {
            return getWebClient().get()
                    .uri("api/participant/teams/" + teamId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            response -> response.bodyToMono(String.class)
                                    .map(msg -> new RuntimeException("Ошибка при получении игры: " + msg)))
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Team>>() {})
                    .block();
        } catch (Exception e) {
            return new ApiResponse<>("Ошибка сервера: " + e.getMessage(), null, true);
        }
    }
}
