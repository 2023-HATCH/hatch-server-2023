package hatch.hatchserver2023.global.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
public class FcmInitializer {

    @Value("${firebase.secret-key.json}")
    private String keyJson;

    @PostConstruct
    public void initialize() throws IOException {
        log.info("FCMInitializer initialize");
        InputStream inputStream = new ByteArrayInputStream(keyJson.getBytes()); // String 으로 받은 환경변수값 json 을 inputstream 으로 변형

        GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId("popo-2023")
                .build();

        if(FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
            log.info("FCMInitializer initialize complete");
        }
    }

    // getInstance() 중복코드 줄여볼까 싶었는데 어떻게 짜야 될지 모르겠음
//    public FirebaseMessaging firebaseMessaging() {
//        return FirebaseMessaging.getInstance(firebaseApp);
//    }
}
