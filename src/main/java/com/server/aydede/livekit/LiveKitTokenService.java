package com.server.aydede.livekit;

import org.springframework.stereotype.Service;

import com.server.aydede.config.LiveKitConfig;
import com.server.aydede.common.exception.LivekitTokenGenerationException;

import io.livekit.server.AccessToken;
import io.livekit.server.CanPublish;
import io.livekit.server.CanSubscribe;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;

@Service
public class LiveKitTokenService {
    private final LiveKitConfig liveKitConfig;

    public LiveKitTokenService(LiveKitConfig liveKitConfig) {
        this.liveKitConfig = liveKitConfig;
    }

    public String generateToken(String name, String identity, String roomName) {
        try {
            AccessToken token = new AccessToken(liveKitConfig.apiKey(), liveKitConfig.apiSecret());
            token.setName(name);
            token.setIdentity(identity);
            token.setTtl(6 * 60);
            token.addGrants(
                    new RoomJoin(true),
                    new RoomName(roomName),
                    new CanPublish(true),
                    new CanSubscribe(true));
            return token.toJwt();
        } catch (Exception e) {
            throw new LivekitTokenGenerationException("Failed to generate token", e);
        }
    }
}
