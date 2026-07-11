package com.server.aydede.voice;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.server.aydede.voice.dto.VoiceTokenResponse;

import jakarta.validation.Valid;

import com.server.aydede.voice.dto.VoiceTokenRequest;
import com.server.aydede.livekit.LiveKitTokenService;
import com.server.aydede.config.LiveKitConfig;

@RestController
@RequestMapping("/api/voice/token")
public class VoiceController {
    private final LiveKitTokenService livekitTokenService;
    private final LiveKitConfig livekitConfig;

    public VoiceController(LiveKitTokenService liveKitTokenService, LiveKitConfig livekitConfig) {
        this.livekitTokenService = liveKitTokenService;
        this.livekitConfig = livekitConfig;
    }

    @PostMapping
    public VoiceTokenResponse getTokenResponse(@AuthenticationPrincipal String uid,
            @Valid @RequestBody(required = false) VoiceTokenRequest request) {
        String roomName = "room-" + UUID.randomUUID();
        String displayName = request.name() != null ? request.name() : uid;
        String token = livekitTokenService.generateToken(displayName, uid, roomName);

        return new VoiceTokenResponse(livekitConfig.url(), token, roomName);
    }
}
