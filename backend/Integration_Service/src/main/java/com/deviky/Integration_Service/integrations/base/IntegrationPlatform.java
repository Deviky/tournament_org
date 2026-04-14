package com.deviky.Integration_Service.integrations.base;

public interface IntegrationPlatform {

    PlatformType getPlatformType();

    StatisticBase getStatistic(String link);
}
