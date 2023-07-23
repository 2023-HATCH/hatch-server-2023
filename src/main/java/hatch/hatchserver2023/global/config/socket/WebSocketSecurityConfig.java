package hatch.hatchserver2023.global.config.socket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

//websocket 에서의 security 설정
@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                .nullDestMatcher().permitAll() //destination 이 없는 CONNECT 등의 요청은 모두 허용
                .simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.HEARTBEAT, SimpMessageType.UNSUBSCRIBE, SimpMessageType.DISCONNECT).permitAll()
                .simpDestMatchers("/app/**").authenticated() //발행 - 인증된 사용자만 허용
                .simpSubscribeDestMatchers("/topic/**").authenticated() //구독 - 인증된 사용자만 허용
                .anyMessage().denyAll() //그 외 모두 비허용
                ;
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true; //개발 중이므로 CSRF 비활성화
    }
}
