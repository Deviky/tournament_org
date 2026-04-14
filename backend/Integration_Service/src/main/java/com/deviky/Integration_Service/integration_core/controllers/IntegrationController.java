package com.deviky.Integration_Service.integration_core.controllers;

import com.deviky.Integration_Service.integration_core.dto.ApiResponse;
import com.deviky.Integration_Service.integration_core.services.IntegrationService;
import com.deviky.Integration_Service.integrations.base.StatisticBase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/integrator")
@RequiredArgsConstructor
public class IntegrationController {

    private final IntegrationService integrationService;

    @PostMapping("/get_player_statistic")
    public ResponseEntity<ApiResponse<List<StatisticBase>>> getPlayerStatistic(
            @RequestBody List<String> links
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>("", integrationService.getStatistics(links), false)
        );
    }
}
