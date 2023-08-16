package hatch.hatchserver2023.global.config.socket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

//@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtWebSocketInterceptor jwtWebSocketInterceptor;
    private final StompExceptionHandler stompExceptionHandler;

    public WebSocketConfig(JwtWebSocketInterceptor jwtWebSocketInterceptor, StompExceptionHandler stompExceptionHandler) {
        this.jwtWebSocketInterceptor = jwtWebSocketInterceptor;
        this.stompExceptionHandler = stompExceptionHandler;
    }


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .setErrorHandler(stompExceptionHandler) // STOMP 단에서 발생하는 에러 잡아서 핸들링
                .addEndpoint("/ws-popo").setAllowedOriginPatterns("*"); //모든 곳으로부터의 요청 허용
//        registry.addEndpoint("/ws-popo").withSockJS(); //socketJS 사용 허용
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); //구독, 전송
        registry.setApplicationDestinationPrefixes("/app"); //발행
    }

    //(jwt 인증) interceptor 추가
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtWebSocketInterceptor);
    }
}
