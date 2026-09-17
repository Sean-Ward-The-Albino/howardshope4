package org.howards4hope.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseAuthConfig {

    @PostConstruct
    public void initializeFirebase() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                return;
            }

            // Attempt to load from classpath (src/main/resources/firebase-service-account.json)
            ClassPathResource serviceAccountResource = new ClassPathResource("firebase-service-account.json");
            
            if (serviceAccountResource.exists()) {
                InputStream serviceAccount = serviceAccountResource.getInputStream();
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println(">>> Firebase Admin SDK initialized successfully with service account credentials.");
            } else {
                // Check if Google Application Default Credentials are available (e.g. Cloud Run / GCP runtime)
                try {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.getApplicationDefault())
                            .setProjectId("howards4hope-b06f6")
                            .build();

                    FirebaseApp.initializeApp(options);
                    System.out.println(">>> Firebase Admin SDK initialized successfully with Google Application Default Credentials (Cloud Run / GCP).");
                } catch (Exception adcEx) {
                    // Graceful fallback for local offline development without crashing server startup!
                    System.out.println(">>> WARNING: firebase-service-account.json not found on classpath and Google ADC unavailable.");
                    System.out.println(">>> Backend is running in MOCK AUTHENTICATION MODE for local rapid development.");
                }
            }
        } catch (Exception e) {
            System.err.println(">>> ERROR: Failed to initialize Firebase Admin SDK: " + e.getMessage());
        }
    }
}
