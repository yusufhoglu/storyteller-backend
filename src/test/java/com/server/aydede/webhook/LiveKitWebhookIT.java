package com.server.aydede.webhook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.server.aydede.persistence.entity.SessionStatus;
import com.server.aydede.persistence.entity.User;
import com.server.aydede.persistence.entity.UserPlan;
import com.server.aydede.persistence.entity.VoiceSession;
import com.server.aydede.persistence.repository.UserRepository;
import com.server.aydede.persistence.repository.VoiceSessionRepository;
import com.server.aydede.support.AbstractIntegrationTest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LiveKitWebhookIT extends AbstractIntegrationTest {

    private static final String API_KEY = "APItestkey";
    private static final String API_SECRET = "testsecretkeyforjwtsigningonly";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoiceSessionRepository voiceSessionRepository;

    @Test
    void roomStarted_marksSessionActive_andSecondCallIsIdempotent() throws Exception {
        String roomName = "room-webhook-" + UUID.randomUUID();
        User user = new User();
        user.setFirebaseUid("webhook-uid-" + UUID.randomUUID());
        user.setAuthProvider("anonymous");
        user.setPlan(UserPlan.FREE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        VoiceSession session = new VoiceSession();
        session.setUser(user);
        session.setRoomName(roomName);
        session.setAgentName("aydede-test");
        session.setStatus(SessionStatus.CREATED);
        session.setCreatedAt(LocalDateTime.now());
        voiceSessionRepository.save(session);

        String body = "{\"event\":\"room_started\",\"room\":{\"name\":\"" + roomName + "\"}}";
        String auth = sign(body);

        mockMvc.perform(post("/webhook/livekit")
                        .contentType("application/webhook+json")
                        .header("Authorization", auth)
                        .content(body))
                .andExpect(status().isOk());

        assertThat(voiceSessionRepository.findByRoomName(roomName)).isPresent().get()
                .satisfies(s -> {
                    assertThat(s.getStatus()).isEqualTo(SessionStatus.ACTIVE);
                    assertThat(s.getStartedAt()).isNotNull();
                });

        mockMvc.perform(post("/webhook/livekit")
                        .contentType("application/webhook+json")
                        .header("Authorization", auth)
                        .content(body))
                .andExpect(status().isOk());

        assertThat(voiceSessionRepository.findByRoomName(roomName)).isPresent().get()
                .extracting(VoiceSession::getStatus)
                .isEqualTo(SessionStatus.ACTIVE);
    }

    private static String sign(String body) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String hash = Base64.getEncoder()
                .encodeToString(digest.digest(body.getBytes(StandardCharsets.UTF_8)));
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(API_KEY)
                .withClaim("sha256", hash)
                .withNotBefore(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(300)))
                .sign(Algorithm.HMAC256(API_SECRET));
    }
}
