package com.server.aydede.livekit;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import jakarta.validation.Valid;

import com.server.aydede.voice.VoiceSessionService;
import com.server.aydede.voice.dto.VoiceTokenRequest;
import com.server.aydede.voice.dto.VoiceTokenResponse;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/voice/session")
public class LivekitDispatchController {

    private VoiceSessionService voiceSessionService;

    public LivekitDispatchController(VoiceSessionService voiceSessionService) {
        this.voiceSessionService = voiceSessionService;
    }

    @PostMapping
    public VoiceTokenResponse createSession(
            @AuthenticationPrincipal String uid,
            @Valid @RequestBody(required = false) VoiceTokenRequest request) throws IOException {

        log.info("POST /api/voice/session uid={}", uid);
        return voiceSessionService.createSession(uid, "anonymous", request);

    }
}
