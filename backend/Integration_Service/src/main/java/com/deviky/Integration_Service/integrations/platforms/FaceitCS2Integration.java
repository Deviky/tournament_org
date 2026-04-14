package com.deviky.Integration_Service.integrations.platforms;

import com.deviky.Integration_Service.integrations.base.IntegrationPlatform;
import com.deviky.Integration_Service.integrations.base.PlatformType;
import com.deviky.Integration_Service.integrations.base.StatisticBase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FaceitCS2Integration implements IntegrationPlatform {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${faceit_cs2.api.key}")
    private String apiKey;

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.FACEITCS2;
    }

    @Override
    public StatisticBase getStatistic(String link) {
        String nickname = extractNickname(link);

        String url = "https://open.faceit.com/data/v4/players?nickname=" + nickname;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map body = response.getBody();
        String playerId = (String) body.get("player_id");

        String urlStats = "https://open.faceit.com/data/v4/players/" + playerId + "/stats/cs2";

        HttpEntity<Void> entityStats = new HttpEntity<>(headers);

        ResponseEntity<Map> responseStats = restTemplate.exchange(
                urlStats,
                HttpMethod.GET,
                entityStats,
                Map.class
        );

        Map bodyStats = responseStats.getBody();
        Map games = (Map) body.get("games");
        Map cs2 = (Map) games.get("cs2");
        Map lifetime = (Map) bodyStats.get("lifetime");


        FaceitCS2StatisticDto dto = new FaceitCS2StatisticDto();
        dto.setPlatform("FACEIT");
        dto.setNickname(nickname);
        dto.setElo((Integer) cs2.get("faceit_elo"));
        dto.setLevel((Integer) cs2.get("skill_level"));
        dto.setMatches((String) lifetime.get("Matches"));
        dto.setKd((String) lifetime.get("Average K/D Ratio"));
        dto.setAdr((String) lifetime.get("ADR"));
        dto.setWinRate((String) lifetime.get("Win Rate %") + "%");

        return dto;
    }

    private String extractNickname(String link) {
        return link.substring(link.lastIndexOf("/") + 1);
    }
}