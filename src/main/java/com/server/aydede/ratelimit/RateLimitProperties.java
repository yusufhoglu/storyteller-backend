package com.server.aydede.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(int userPerMinute, int userPerHour, int ipPerMinute) {

}
