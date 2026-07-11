package com.server.aydede.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;
import java.util.Map;
import java.io.ByteArrayInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.LinkedHashMap;
import com.google.api.client.http.javanet.NetHttpTransport;

@Configuration
@EnableConfigurationProperties(FirebaseProperties.class)
public class FirebaseConfig {

    private final FirebaseProperties properties;

    public FirebaseConfig(FirebaseProperties properties) {
        this.properties = properties;
    }

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        Map<String, Object> creds = new LinkedHashMap<>();
        creds.put("type", properties.type());
        creds.put("project_id", properties.projectId());
        creds.put("private_key_id", properties.privateKeyId());
        creds.put("private_key", properties.privateKey().replace("\\n", "\n"));
        creds.put("client_email", properties.clientEmail());
        creds.put("client_id", properties.clientId());
        creds.put("auth_uri", properties.authUri());
        creds.put("token_uri", properties.tokenUri());
        creds.put("auth_provider_x509_cert_url", properties.authProviderX509CertUrl());
        creds.put("client_x509_cert_url", properties.clientX509CertUrl());
        creds.put("universe_domain", properties.universeDomain());
        ByteArrayInputStream stream = new ByteArrayInputStream(
                new ObjectMapper().writeValueAsBytes(creds));
        FirebaseOptions options = FirebaseOptions.builder()
                .setHttpTransport(new NetHttpTransport())
                .setCredentials(GoogleCredentials.fromStream(stream))
                .build();
        return FirebaseApp.initializeApp(options);
    }

}
