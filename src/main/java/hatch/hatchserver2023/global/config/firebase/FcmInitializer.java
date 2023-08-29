package hatch.hatchserver2023.global.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.stream.JsonReader;
import hatch.hatchserver2023.global.common.ObjectMapperUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;

@Slf4j
@Component
public class FcmInitializer {

//    @Value("${firebase.secret-key.json}")
//    private String keyJson;

    @Value("${firebase.secret-key.path}")
    private String filePath;

    @PostConstruct
    public void initialize() throws IOException {
        log.info("FCMInitializer initialize");

        // 리눅스에서 에러남
//        InputStream inputStream = new ByteArrayInputStream(keyJson.getBytes()); // String 으로 받은 환경변수값 json 을 inputstream 으로 변형

        // resoutces 파일 바로 접근할 수 있는 방법임 -> 리눅스에서 불가
//        ClassPathResource resource = new ClassPathResource(filePath);
//        InputStream inputStream = resource.getInputStream();

        // 절대경로로 파일 읽어오기
        String basePath = new File("").getAbsolutePath();
//        log.info("basePath : {}", basePath);
        String fileAbsolutePath = basePath + filePath;
//        log.info("fileAbsolutePath : {}", fileAbsolutePath);

        InputStream inputStream = new FileInputStream(new File(fileAbsolutePath));

        GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId("poket-pose") //프로젝트 생성할 때 오타나서 프로젝트명 poket이 맞음
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
