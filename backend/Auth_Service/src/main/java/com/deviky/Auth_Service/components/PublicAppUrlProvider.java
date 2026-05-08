package com.deviky.Auth_Service.components;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PublicAppUrlProvider {

    @Value("${app.public-url:}")
    private String configuredPublicUrl;

    public String resolve(HttpServletRequest request) {
        if (StringUtils.hasText(configuredPublicUrl)) {
            return trimTrailingSlash(configuredPublicUrl.trim());
        }

        String forwardedProto = firstHeaderValue(request.getHeader("X-Forwarded-Proto"));
        String forwardedHost = firstHeaderValue(request.getHeader("X-Forwarded-Host"));

        String scheme = StringUtils.hasText(forwardedProto) ? forwardedProto : request.getScheme();

        if (StringUtils.hasText(forwardedHost)) {
            return scheme + "://" + trimTrailingSlash(forwardedHost);
        }

        StringBuilder appUrl = new StringBuilder()
                .append(request.getScheme())
                .append("://")
                .append(request.getServerName());

        if (!isDefaultPort(request.getScheme(), request.getServerPort())) {
            appUrl.append(":").append(request.getServerPort());
        }

        return appUrl.toString();
    }

    private String firstHeaderValue(String headerValue) {
        if (!StringUtils.hasText(headerValue)) {
            return null;
        }

        return headerValue.split(",")[0].trim();
    }

    private boolean isDefaultPort(String scheme, int port) {
        return ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
