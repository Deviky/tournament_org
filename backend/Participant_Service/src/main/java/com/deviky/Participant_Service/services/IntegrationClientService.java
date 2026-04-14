package com.deviky.Participant_Service.services;

import com.deviky.Participant_Service.dto.ApiResponse;
import com.deviky.Participant_Service.dto.Game;
import com.deviky.Participant_Service.dto.TeamDto;
import com.deviky.Participant_Service.models.Player;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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
public class IntegrationClientService {
    private final WebClient.Builder webClientBuilder;
    private WebClient getWebClient() {
        return webClientBuilder.baseUrl("http://INTEGRATION-SERVICE").build();
    }

    public ApiResponse<List<JsonNode>> getPlayerStatistic(List<String> urls) {
        try {
            return getWebClient().post()
                    .uri("/api/integrator/get_player_statistic")
                    .bodyValue(urls)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<JsonNode>>>() {})
                    .block();
        } catch (Exception ex) {
            return null;
        }
    }
}
