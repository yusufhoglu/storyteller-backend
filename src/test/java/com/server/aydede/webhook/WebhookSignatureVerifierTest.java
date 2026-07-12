package com.server.aydede.webhook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import livekit.LivekitWebhook.WebhookEvent;

class WebhookSignatureVerifierTest {

    private static final String API_KEY = "APItestkey";
    private static final String API_SECRET = "testsecretkeyforjwtsigningonly";
    private static final String BODY = "{\"event\":\"room_started\",\"room\":{\"name\":\"room-1\"}}";

    private WebhookSignatureVerifier verifier;

    @BeforeEach
    void setUp() {
        verifier = new WebhookSignatureVerifier(API_KEY, API_SECRET);
    }

    @Test
    void verifyAndParse_acceptsValidSignature() throws Exception {
        String auth = sign(BODY, API_SECRET, Instant.now());

        WebhookEvent event = verifier.verifyAndParse(BODY, auth);

        assertThat(event.getEvent()).isEqualTo("room_started");
        assertThat(event.getRoom().getName()).isEqualTo("room-1");
    }

    @Test
    void verifyAndParse_rejectsWrongSecret() throws Exception {
        String auth = sign(BODY, "wrong-secret-value-xxxxx", Instant.now());

        assertThatThrownBy(() -> verifier.verifyAndParse(BODY, auth))
                .isInstanceOf(Exception.class);
    }

    @Test
    void verifyAndParse_rejectsTamperedBody() throws Exception {
        String auth = sign(BODY, API_SECRET, Instant.now());
        String tampered = BODY.replace("room-1", "room-hacked");

        assertThatThrownBy(() -> verifier.verifyAndParse(tampered, auth))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sha256");
    }

    @Test
    void verifyAndParse_acceptsTokenWithFutureNbfWithinLeeway() throws Exception {
        Instant slightlyFuture = Instant.now().plusSeconds(2);
        String auth = sign(BODY, API_SECRET, slightlyFuture);

        WebhookEvent event = verifier.verifyAndParse(BODY, auth);

        assertThat(event.getEvent()).isEqualTo("room_started");
    }

    private static String sign(String body, String secret, Instant nbf) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String hash = Base64.getEncoder()
                .encodeToString(digest.digest(body.getBytes(StandardCharsets.UTF_8)));

        return JWT.create()
                .withIssuer(API_KEY)
                .withClaim("sha256", hash)
                .withNotBefore(Date.from(nbf))
                .withExpiresAt(Date.from(nbf.plusSeconds(300)))
                .sign(Algorithm.HMAC256(secret));
    }
}
