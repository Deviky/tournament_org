package com.deviky.Integration_Service.integration_core.services;

import com.deviky.Integration_Service.integrations.base.PlatformType;
import org.springframework.stereotype.Component;

@Component
public class PlatformDetector {

    public PlatformType detect(String link) {
        if (link.contains("faceit.com")) {
            return PlatformType.FACEITCS2;
        }

        return PlatformType.UNKNOWN;
    }
}