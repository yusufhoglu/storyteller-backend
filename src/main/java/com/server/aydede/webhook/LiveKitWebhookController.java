package com.server.aydede.webhook;

import java.io.IOException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.server.aydede.common.exception.WebhookVerificationException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/webhook/livekit")
public class LiveKitWebhookController {
    private final LivekitWebookService livekitWebookService;

    public LiveKitWebhookController(LivekitWebookService livekitWebookService) {
        this.livekitWebookService = livekitWebookService;
    }

    @PostMapping(consumes = "application/webhook+json")
    public ResponseEntity<String> receiveWebhook(HttpServletRequest request) throws IOException {
        byte[] bodyBytes = request.getInputStream().readAllBytes();
        String authHeader = request.getHeader("Authorization");

        log.info("LiveKit webhook received, bodyLength={}, hasAuthHeader={}",
                bodyBytes.length, authHeader != null && !authHeader.isBlank());
        try {
            livekitWebookService.handleEvent(bodyBytes, authHeader);
            log.info("LiveKit webhook processed successfully");
            return ResponseEntity.ok("Webhook received successfully");
        } catch (WebhookVerificationException e) {
            String reason = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            log.warn("LiveKit webhook verification failed: {}", reason);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid signature");
        }
    }
}
