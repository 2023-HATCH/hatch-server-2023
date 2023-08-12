package hatch.hatchserver2023;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.TimeZone;
@EnableScheduling // 일정 시간마다 작업 실행
@EnableAsync // 비동기 처리
@EnableJpaAuditing
@ServletComponentScan // Filter 등록
@SpringBootApplication
public class HatchServer2023Application {

	@PostConstruct
	public void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}

	public static void main(String[] args) {
		SpringApplication.run(HatchServer2023Application.class, args);
	}

}
