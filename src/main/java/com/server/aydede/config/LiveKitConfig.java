package com.server.aydede.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "livekit")
public record LiveKitConfig(String url, String apiKey, String apiSecret) {

}