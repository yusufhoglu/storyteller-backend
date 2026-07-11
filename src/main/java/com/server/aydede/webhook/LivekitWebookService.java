package com.server.aydede.webhook;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.server.aydede.config.LiveKitConfig;
import com.server.aydede.common.exception.WebhookVerificationException;
import com.server.aydede.persistence.entity.SessionStatus;
import com.server.aydede.persistence.repository.VoiceSessionRepository;

import io.livekit.server.WebhookReceiver;
import livekit.LivekitWebhook.WebhookEvent;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LivekitWebookService {
    private final WebhookReceiver webhookReceiver;
    private final VoiceSessionRepository voiceSessionRepository;

    public LivekitWebookService(LiveKitConfig config, VoiceSessionRepository voiceSessionRepository) {
        this.webhookReceiver = new WebhookReceiver(config.apiKey(), config.apiSecret());
        this.voiceSessionRepository = voiceSessionRepository;
    }

    @Transactional
    public void handleEvent(byte[] bodyBytes, String authHeader) {
        String body = new String(bodyBytes, StandardCharsets.UTF_8);
        String normalizedAuthHeader = normalizeAuthHeader(authHeader);

        WebhookEvent event;
        try {
            event = webhookReceiver.receive(body, normalizedAuthHeader);
        } catch (Exception e) {
            log.warn("WebhookReceiver failed: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            throw new WebhookVerificationException("Invalid webhook: " + e.getMessage(), e);
        }

        String eventType = event.getEvent();
        if (!event.hasRoom()) {
            log.info("LiveKit webhook ignored: event={} (no room)", eventType);
            return;
        }

        String roomName = event.getRoom().getName();
        log.info("LiveKit webhook event={} room={}", eventType, roomName);
        switch (eventType) {
            case "room_started" -> markActive(roomName);
            case "room_finished" -> markEnded(roomName);
            default -> log.info("LiveKit webhook ignored: unhandled event={} room={}", eventType, roomName);
        }
    }

    private void markActive(String roomName) {
        voiceSessionRepository.findByRoomName(roomName).ifPresentOrElse(session -> {
            if (session.getStatus() == SessionStatus.ACTIVE) {
                log.info("VoiceSession already ACTIVE, skipping: room={}", roomName);
                return;
            }
            session.setStatus(SessionStatus.ACTIVE);
            if (session.getStartedAt() == null) {
                session.setStartedAt(LocalDateTime.now());
            }
            voiceSessionRepository.save(session);
            log.info("VoiceSession marked ACTIVE: room={}", roomName);
        }, () -> log.warn("VoiceSession not found for room_started: room={}", roomName));
    }

    private void markEnded(String roomName) {
        voiceSessionRepository.findByRoomName(roomName).ifPresentOrElse(session -> {
            if (session.getStatus() == SessionStatus.ENDED) {
                log.info("VoiceSession already ENDED, skipping: room={}", roomName);
                return;
            }
            LocalDateTime endedAt = LocalDateTime.now();
            session.setStatus(SessionStatus.ENDED);
            session.setEndedAt(endedAt);
            if (session.getStartedAt() != null) {
                long seconds = Duration.between(session.getStartedAt(), endedAt).getSeconds();
                session.setDurationSeconds((int) seconds);
            } else if (session.getCreatedAt() != null) {
                long seconds = Duration.between(session.getCreatedAt(), endedAt).getSeconds();
                session.setDurationSeconds((int) seconds);
            }
            voiceSessionRepository.save(session);
            log.info("VoiceSession marked ENDED: room={} durationSeconds={}", roomName, session.getDurationSeconds());
        }, () -> log.warn("VoiceSession not found for room_finished: room={}", roomName));
    }

    private String normalizeAuthHeader(String authHeader) {
        if (authHeader == null) {
            return null;
        }
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }
}