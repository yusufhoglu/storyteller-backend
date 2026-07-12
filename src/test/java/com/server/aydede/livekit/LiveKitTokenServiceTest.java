package com.server.aydede.livekit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.server.aydede.config.LiveKitConfig;

class LiveKitTokenServiceTest {

    private static final String API_KEY = "APItestkey";
    private static final String API_SECRET = "testsecretkeyforjwtsigningonly";

    private LiveKitTokenService tokenService;

    @BeforeEach
    void setUp() {
        LiveKitConfig config = new LiveKitConfig(
                "wss://test.livekit.cloud",
                API_KEY,
                API_SECRET,
                "aydede-test",
                "https://test.livekit.cloud");
        tokenService = new LiveKitTokenService(config);
    }

    @Test
    void generateToken_setsIdentityNameRoomGrantsAndTtl() {
        String token = tokenService.generateToken("Ada", "uid-123", "room-abc");

        DecodedJWT jwt = JWT.decode(token);

        assertThat(jwt.getIssuer()).isEqualTo(API_KEY);
        assertThat(jwt.getSubject()).isEqualTo("uid-123");
        assertThat(jwt.getClaim("name").asString()).isEqualTo("Ada");

        var video = jwt.getClaim("video").asMap();
        assertThat(video).isNotNull();
        assertThat(video.get("roomJoin")).isEqualTo(true);
        assertThat(video.get("room")).isEqualTo("room-abc");
        assertThat(video.get("canPublish")).isEqualTo(true);
        assertThat(video.get("canSubscribe")).isEqualTo(true);

        // Signature must match our api secret
        JWT.require(Algorithm.HMAC256(API_SECRET))
                .withIssuer(API_KEY)
                .acceptLeeway(60)
                .build()
                .verify(token);

        assertThat(jwt.getExpiresAt()).isNotNull();
        assertThat(jwt.getExpiresAt().toInstant())
                .isAfter(Instant.now().minusSeconds(30))
                .isBefore(Instant.now().plusSeconds(7 * 60));
    }
}
