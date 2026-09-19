package com.ticketing.platform.shared.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Configuration
public class FirebaseConfig {

    private boolean firebaseInitialized = false;

    @PostConstruct
    public void initFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            firebaseInitialized = true;
            return;
        }

        try {
            InputStream serviceAccountStream = null;

            // 1. Check environment variable (file path or JSON content)
            String envConfig = System.getenv("FIREBASE_CONFIG_CREDENTIALS");
            if (envConfig != null && !envConfig.isBlank()) {
                if (envConfig.trim().startsWith("{")) {
                    serviceAccountStream = new ByteArrayInputStream(envConfig.getBytes(StandardCharsets.UTF_8));
                } else if (Files.exists(Path.of(envConfig))) {
                    serviceAccountStream = Files.newInputStream(Path.of(envConfig));
                }
            }

            // 2. Check classpath resource
            if (serviceAccountStream == null) {
                ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
                if (resource.exists()) {
                    serviceAccountStream = resource.getInputStream();
                }
            }

            if (serviceAccountStream != null) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                        .build();
                FirebaseApp.initializeApp(options);
                firebaseInitialized = true;
                log.info("Firebase Admin SDK successfully initialized.");
            } else {
                log.warn("Firebase credentials not found. Running in Dev/Mock Auth mode.");
            }
        } catch (Exception e) {
            log.warn("Failed to initialize Firebase Admin SDK (will fallback to Dev/Mock mode): {}", e.getMessage());
        }
    }

    public boolean isFirebaseInitialized() {
        return firebaseInitialized && !FirebaseApp.getApps().isEmpty();
    }

    @Bean
    public FirebaseAuth firebaseAuth() {
        if (isFirebaseInitialized()) {
            return FirebaseAuth.getInstance();
        }
        return null;
    }
}
