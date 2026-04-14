package com.deviky.Integration_Service.integration_core.services;

import com.deviky.Integration_Service.integrations.base.IntegrationPlatform;
import com.deviky.Integration_Service.integrations.base.StatisticBase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IntegrationService {

    private final IntegrationFactory factory;

    public List<StatisticBase> getStatistics(List<String> links) {
        List<StatisticBase> result = new ArrayList<>();

        for (String link : links) {
            factory.getIntegration(link)
                    .ifPresent(platform ->
                            result.add(platform.getStatistic(link))
                    );
        }

        return result;
    }
}