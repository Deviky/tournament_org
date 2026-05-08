package com.deviky.Match_Service.services;

import com.deviky.Match_Service.dto.ApiResponse;
import com.deviky.Match_Service.dto.MatchResultDto;
import com.deviky.Match_Service.dto.Team;
import com.deviky.Match_Service.models.MatchTeamResult;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentClientService {
    private final WebClient.Builder webClientBuilder;

    private WebClient getWebClient() {
        return webClientBuilder.baseUrl("http://TOURNAMENT-SERVICE").build();
    }

    public ApiResponse<Void> checkTournamentInfo(Long tournamentId, Long organizerId) {
        try {
            return getWebClient().get()  // Используем GET, как на сервере
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/tournaments/private/create_match_check/" + tournamentId)
                            .queryParam("organizerId", organizerId)  // Передаем как query параметр
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            response -> response.bodyToMono(String.class)
                                    .map(msg -> new RuntimeException("Ошибка при проверке турнира: " + msg)))
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Void>>() {})
                    .block();
        } catch (Exception e) {
            return new ApiResponse<>("Ошибка сервера: " + e.getMessage(), null, true);
        }
    }

    public ApiResponse<Void> updateBracket(Long tournamentId, MatchResultDto matchResult) {
        try {
            return getWebClient().post()
                    .uri("/api/tournaments/organizer/bracket/update/{tournamentId}", tournamentId)
                    .bodyValue(matchResult)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            response -> response.bodyToMono(String.class)
                                    .map(msg -> new RuntimeException("Ошибка обновления сетки: " + msg)))
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Void>>() {})
                    .block();

        } catch (Exception e) {
            return new ApiResponse<>("Ошибка сервера: " + e.getMessage(), null, true);
        }
    }

    public ApiResponse<Void> cancelMatchUpdateBracket(Long tournamentId, Long matchId) {

        try {

            return getWebClient().post()
                    .uri("/api/tournaments/private/bracket/match_cancel/" + tournamentId + "?matchId=" + matchId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Void>>() {})
                    .block();

        } catch (Exception e) {
            return new ApiResponse<>("Ошибка обновления сетки", null, true);
        }
    }
}
