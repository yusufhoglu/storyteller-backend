package com.server.aydede.webhook;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.protobuf.util.JsonFormat;

import livekit.LivekitWebhook.WebhookEvent;

/**
 * Verifies LiveKit webhook JWT + body checksum, then parses the event.
 */
public class WebhookSignatureVerifier {

    private static final int CLOCK_LEEWAY_SECONDS = 10;

    private final String apiKey;
    private final String apiSecret;

    public WebhookSignatureVerifier(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public WebhookEvent verifyAndParse(String body, String authHeader) throws Exception {
        if (authHeader == null || authHeader.isBlank()) {
            throw new IllegalArgumentException("Auth header is blank");
        }

        Algorithm algorithm = Algorithm.HMAC256(apiSecret);
        DecodedJWT decodedJwt = JWT.require(algorithm)
                .withIssuer(apiKey)
                .acceptLeeway(CLOCK_LEEWAY_SECONDS)
                .build()
                .verify(authHeader);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String bodyHash = Base64.getEncoder()
                .encodeToString(digest.digest(body.getBytes(StandardCharsets.UTF_8)));
        String claimHash = decodedJwt.getClaim("sha256").asString();
        if (!bodyHash.equals(claimHash)) {
            throw new IllegalArgumentException("sha256 checksum of body does not match!");
        }

        WebhookEvent.Builder builder = WebhookEvent.newBuilder();
        JsonFormat.parser().ignoringUnknownFields().merge(body, builder);
        return builder.build();
    }
}
