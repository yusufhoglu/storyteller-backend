package com.server.aydede.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "config")
public record ConfigProperties(String allowedOrigins) {
}
