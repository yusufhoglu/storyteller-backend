package com.server.aydede.voice;

import java.net.ResponseCache;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.server.aydede.voice.dto.VoiceTokenResponse;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import com.server.aydede.voice.dto.VoiceTokenRequest;
import com.server.aydede.livekit.LiveKitTokenService;
import com.server.aydede.config.LiveKitConfig;

@Slf4j
@RestController
@RequestMapping("/api/voice/token")
@Deprecated(forRemoval = true)
public class VoiceController {
    private final LiveKitTokenService livekitTokenService;
    private final LiveKitConfig livekitConfig;

    public VoiceController(LiveKitTokenService liveKitTokenService, LiveKitConfig livekitConfig) {
        this.livekitTokenService = liveKitTokenService;
        this.livekitConfig = livekitConfig;
    }

    @PostMapping
    @Deprecated(forRemoval = true)
    public ResponseEntity<VoiceTokenResponse> getTokenResponse(@AuthenticationPrincipal String uid,
            @Valid @RequestBody(required = false) VoiceTokenRequest request) {
        log.warn("This endpoint is deprecated and will be removed on 2026-07-13");
        String roomName = "room-" + UUID.randomUUID();
        String displayName = request != null && request.name() != null ? request.name() : uid;
        String token = livekitTokenService.generateToken(displayName, uid, roomName);

        VoiceTokenResponse body = new VoiceTokenResponse(livekitConfig.url(), token, roomName);

        return ResponseEntity.ok()
                .header("Deprecation", "true")
                .header("Link", "</api/voice/session>; rel=\"successor-version\"")
                .header("Sunset", "Sat, 13 Jul 2026 00:00:00 GMT")
                .body(body);
    }
}
