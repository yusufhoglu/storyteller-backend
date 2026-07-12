package com.server.aydede.voice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.server.aydede.livekit.LivekitDispatchService;
import com.server.aydede.persistence.entity.SessionStatus;
import com.server.aydede.persistence.repository.UserRepository;
import com.server.aydede.persistence.repository.VoiceSessionRepository;
import com.server.aydede.support.AbstractIntegrationTest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VoiceSessionIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VoiceSessionRepository voiceSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private LivekitDispatchService livekitDispatchService;

    @BeforeEach
    void setUp() throws Exception {
        doNothing().when(livekitDispatchService).dispatchAgent(anyString(), anyString(), any());
    }

    @Test
    void createSession_persistsCreatedSessionAndReturnsToken() throws Exception {
        String uid = "session-it-uid-" + System.nanoTime();

        MvcResult result = mockMvc.perform(post("/api/voice/session")
                        .with(authentication(auth(uid)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Tester\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("wss://test.livekit.cloud"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.roomName").value(org.hamcrest.Matchers.startsWith("room-")))
                .andReturn();

        String roomName = com.jayway.jsonpath.JsonPath.read(
                result.getResponse().getContentAsString(), "$.roomName");

        assertThat(voiceSessionRepository.findByRoomName(roomName)).isPresent().get()
                .extracting(s -> s.getStatus())
                .isEqualTo(SessionStatus.CREATED);
        assertThat(userRepository.findByFirebaseUid(uid)).isPresent();

        verify(livekitDispatchService).dispatchAgent(anyString(), anyString(), any());
    }
}
