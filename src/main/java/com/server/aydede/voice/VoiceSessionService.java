package com.server.aydede.voice;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.server.aydede.persistence.repository.UserRepository;
import com.server.aydede.persistence.repository.VoiceSessionRepository;
import com.server.aydede.voice.dto.VoiceTokenRequest;
import com.server.aydede.voice.dto.VoiceTokenResponse;
import com.server.aydede.persistence.entity.*;
import jakarta.transaction.Transactional;
import com.server.aydede.livekit.LivekitDispatchMetada;
import com.server.aydede.livekit.LivekitDispatchService;
import com.server.aydede.livekit.LiveKitTokenService;
import com.server.aydede.config.LiveKitConfig;
import com.server.aydede.common.exception.QuotaExceededException;
import tools.jackson.databind.json.JsonMapper;
import java.util.UUID;
import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class VoiceSessionService {
    private final UserRepository userRepository;
    private final VoiceSessionRepository voiceSessionRepository;
    private final LivekitDispatchService livekitDispatchService;
    private final LiveKitTokenService liveKitTokenService;
    private final LiveKitConfig liveKitConfig;
    private final JsonMapper jsonMapper;

    public VoiceSessionService(UserRepository userRepository, VoiceSessionRepository voiceSessionRepository,
            LivekitDispatchService livekitDispatchService, LiveKitTokenService liveKitTokenService,
            LiveKitConfig liveKitConfig, JsonMapper jsonMapper) {
        this.userRepository = userRepository;
        this.voiceSessionRepository = voiceSessionRepository;
        this.livekitDispatchService = livekitDispatchService;
        this.liveKitTokenService = liveKitTokenService;
        this.liveKitConfig = liveKitConfig;
        this.jsonMapper = jsonMapper;
    }

    private User createUser(String firebaseUid, String authProvider) {
        User user = new User();
        user.setFirebaseUid(firebaseUid);
        user.setAuthProvider(authProvider);
        user.setPlan(UserPlan.FREE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        log.info("Created new user: firebaseUid={} authProvider={}", firebaseUid, authProvider);
        return saved;
    }

    public VoiceTokenResponse createSession(String firebaseUid, String authProvider,
            VoiceTokenRequest request) throws IOException {

        log.info("Creating voice session: firebaseUid={} authProvider={}", firebaseUid, authProvider);

        // user upsert
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseGet(() -> createUser(firebaseUid, authProvider));

        // Limit
        if (user.getPlan() == UserPlan.FREE) {
            long used = voiceSessionRepository.countByUser(user);
            log.info("Quota check: firebaseUid={} usedSessions={}", firebaseUid, used);
            if (used >= 10) {
                log.warn("Quota exceeded: firebaseUid={} usedSessions={}", firebaseUid, used);
                throw new QuotaExceededException("User has reached the limit of voice sessions");
            }
        }

        // Room+metadata
        String roomName = "room-" + UUID.randomUUID();
        String displayName = request != null && request.name() != null ? request.name() : firebaseUid;

        LivekitDispatchMetada metadata = new LivekitDispatchMetada(
                firebaseUid,
                displayName,
                request != null ? request.language() : null,
                request != null ? request.gender() : null,
                request != null ? request.age() : null,
                request != null ? request.favBooks() : null);

        VoiceSession session = new VoiceSession();
        session.setUser(user);
        session.setRoomName(roomName);
        session.setAgentName(liveKitConfig.agentName());
        session.setStatus(SessionStatus.CREATED);
        session.setMetadataJson(jsonMapper.writeValueAsString(metadata));
        session.setCreatedAt(LocalDateTime.now());
        voiceSessionRepository.save(session);

        livekitDispatchService.dispatchAgent(roomName, firebaseUid, metadata);
        String token = liveKitTokenService.generateToken(displayName, firebaseUid, roomName);
        log.info("Voice session created: firebaseUid={} room={} status=CREATED", firebaseUid, roomName);
        return new VoiceTokenResponse(liveKitConfig.url(), token, roomName);

    }
}
