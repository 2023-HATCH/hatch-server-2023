package hatch.hatchserver2023.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/docs/**") // 이 url 경로에 접근하면
                .addResourceLocations("classpath:/static/docs/"); // 이 실제 경로에 있는 파일을 찾아 반환하도록 설정
        // ex) 위와 같이 설정 시 http://localhost:8080/docs/api.html 로 접속하면 resources/static/docs 에 있는 api.html 을 반환함
    }
}
