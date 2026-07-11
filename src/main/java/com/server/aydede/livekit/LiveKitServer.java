package com.server.aydede.livekit;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import com.server.aydede.config.LiveKitConfig;

@Component
public class LiveKitServer {
    private final LiveKitConfig liveKitConfig;

    public LiveKitServer(LiveKitConfig liveKitConfig) {
        this.liveKitConfig = liveKitConfig;
    }

    @PostConstruct
    public void start() {
        System.out.println("LiveKit server started on " + liveKitConfig.url());
        // System.out.println("LiveKit API key: " + liveKitConfig.apiKey());
        // System.out.println("LiveKit API secret: " + liveKitConfig.apiSecret());
    }
}
