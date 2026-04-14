package com.deviky.Tournament_Service.tournament_core.services;


import com.deviky.Tournament_Service.tournament_core.dto.ApiResponse;
import com.deviky.Tournament_Service.tournament_core.dto.CreateMatchDto;
import com.deviky.Tournament_Service.tournament_core.dto.Match;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MatchClientService {

    private final WebClient.Builder webClientBuilder;

    private WebClient getWebClient() {
        return webClientBuilder.baseUrl("http://MATCH-SERVICE").build();
    }

    public ApiResponse<Map<Long, Match>> createMatchesByBracket(
            Map<Long, CreateMatchDto> matches) {

        try {
            return getWebClient().post()
                    .uri("/api/matches/private/create_by_batch")
                    .bodyValue(matches)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            response -> response.bodyToMono(String.class)
                                    .map(msg -> new RuntimeException("Ошибка создания матчей: " + msg)))
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Map<Long, Match>>>() {})
                    .block();

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании матчей: " + e.getMessage());
        }
    }

    public ApiResponse<List<Match>> getMatchesByTournament(Long tournamentId) {

        try {
            return getWebClient().get()
                    .uri("/api/matches/public/get_by_tournament/" + tournamentId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<Match>>>() {})
                    .block();

        } catch (Exception e) {
            return new ApiResponse<>("Ошибка получения матчей: " + e.getMessage(), null, true);
        }
    }

    public ApiResponse<Void> cancelMatchesByTournament(Long tournamentId) {
        try {
            return getWebClient().post()
                    .uri("/api/matches/private/by_tournament/cancel/{tournamentId}", tournamentId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            response -> response.bodyToMono(String.class)
                                    .map(msg -> new RuntimeException("Ошибка отмены матчей: " + msg)))
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Void>>() {})
                    .block();
        } catch (Exception e) {
            return new ApiResponse<>("Ошибка при отмене матчей турнира: " + e.getMessage(), null, true);
        }
    }
}
