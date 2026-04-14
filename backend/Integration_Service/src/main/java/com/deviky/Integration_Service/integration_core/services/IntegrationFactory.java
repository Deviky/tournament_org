package com.deviky.Integration_Service.integration_core.services;

import com.deviky.Integration_Service.integrations.base.IntegrationPlatform;
import com.deviky.Integration_Service.integrations.base.PlatformType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IntegrationFactory {

    private final List<IntegrationPlatform> platforms;
    private final PlatformDetector detector;

    public Optional<IntegrationPlatform> getIntegration(String link) {
        PlatformType type = detector.detect(link);

        return platforms.stream()
                .filter(p -> p.getPlatformType() == type)
                .findFirst();
    }
}
