package com.server.aydede.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;
import java.util.Map;
import java.io.ByteArrayInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@ConfigurationProperties(prefix = "firebase")
public record FirebaseProperties(String type, String projectId, String privateKeyId, String privateKey,
        String clientEmail,
        String clientId, String authUri, String tokenUri, String authProviderX509CertUrl, String clientX509CertUrl,
        String universeDomain) {
}
