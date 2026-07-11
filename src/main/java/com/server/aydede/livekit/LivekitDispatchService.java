package com.server.aydede.livekit;

import java.io.IOException;

import org.springframework.stereotype.Service;
import livekit.LivekitAgentDispatch;
import com.server.aydede.common.exception.LivekitTokenGenerationException;
import com.server.aydede.config.LiveKitConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.livekit.server.AgentDispatchServiceClient;
import retrofit2.Response;

@Service
public class LivekitDispatchService {
    private final AgentDispatchServiceClient client;
    private final LiveKitConfig config;
    private final ObjectMapper objectMapper;

    public LivekitDispatchService(LiveKitConfig config) {
        this.config = config;
        this.client = AgentDispatchServiceClient.createClient(
                config.apiHost(),
                config.apiKey(),
                config.apiSecret());
        this.objectMapper = new ObjectMapper();
    }

    public void dispatchAgent(String roomName, String userId, LivekitDispatchMetada metadata) throws IOException {
        String json = objectMapper.writeValueAsString(metadata);

        Response<LivekitAgentDispatch.AgentDispatch> response = client
                .createDispatch(roomName, config.agentName(), json).execute();
        if (!response.isSuccessful()) {
            throw new LivekitTokenGenerationException("Agent dispatch failed: " + response.code(), null);
        }
    }
}
