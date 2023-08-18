package hatch.hatchserver2023.global.config.socket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    /**
     * 소켓 연결 시 세션 식별자UUID를 생성해 반환해주는 메서드. 이 메서드가 handShake 시 스프링 내에서 소켓연결에 사용됨
     * @param request the handshake request
     * @param wsHandler the WebSocket handler that will handle messages
     * @param attributes handshake attributes to pass to the WebSocket session
     * @return
     */
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
//        log.info("[HANDLER] CustomHandshakeHandler determineUser");
        return new StompPrincipal(UUID.randomUUID().toString());
//        return super.determineUser(request, wsHandler, attributes);
    }
}
