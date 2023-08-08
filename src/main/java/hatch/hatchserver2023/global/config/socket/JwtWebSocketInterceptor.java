package hatch.hatchserver2023.global.config.socket;

import hatch.hatchserver2023.domain.stage.application.StageService;
import hatch.hatchserver2023.domain.stage.application.StageSocketService;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.code.StageStatusCode;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import hatch.hatchserver2023.global.common.response.exception.AuthException;
import hatch.hatchserver2023.global.common.response.exception.StageException;
import hatch.hatchserver2023.global.config.security.jwt.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99) //인터셉터들 사이의 우선순위. jwt로직이 security설정보다 앞서야 함. 가장 높은 우선순위에 +99 하여 확실히 제일 높게 함
// 소켓에서는 토큰 RTR 적용 일단 안함
public class JwtWebSocketInterceptor implements ChannelInterceptor {
    private final JwtProvider jwtProvider;
    private final StageSocketService stageSocketService;

    public JwtWebSocketInterceptor(JwtProvider jwtProvider, StageSocketService stageSocketService) {
        this.jwtProvider = jwtProvider;
        this.stageSocketService = stageSocketService;
    }



    /**
     * 메세지가 다른 사용자에게 발행되기 전에 실행되는 preSend 메서드. JWT 인증 로직
     * @param message : 소켓 요청 내용 전체
     * @param channel
     * @return message
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        log.info("[INTERCEPTOR] WebSocketInterceptor preSend");
//        log.info("message : {}", message);
//        log.info("native header : {}", headerAccessor.getHeader("nativeHeaders"));
//        log.info("native header - Authorization : {}", headerAccessor.getFirstNativeHeader("Authorization"));

        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        //stomp 형식에서 요청 타입인 COMMAND 부분 값을 가져옴
        StompCommand command = headerAccessor.getCommand();

        //TODO : 만약 메세지 발행 시에는 토큰 확인 안하게 될 경우 이 부분 수정
        //첫 연결 시, 메세지 발행 시에 로직 실행하도록 함 //TODO : 구독 시에는 어떻게 헤더 다는지 난 잘 모르겠음. 프론트와 논의
        if(StompCommand.CONNECT == command || StompCommand.SEND == command) { //|| StompCommand.SUBSCRIBE == command // || StompCommand.MESSAGE == command
            String token = headerAccessor.getFirstNativeHeader(jwtProvider.ACCESS_TOKEN_NAME);

            //토큰 유효기간 확인 // TODO : 이거 예외 핸들러로 잡아주기!!!!!!! 예외응답!! -> 어떻게 하는 거지..? 공부하기
            if(!jwtProvider.isTokenValid(token)){
//                log.info("[INTERCEPTOR] WebSocketInterceptor : this token is invalid");
                throw new AuthException(UserStatusCode.TOKEN_CANNOT_RESOLVE);
            }

            // simpHeartbeat 내의 simpUser 라는 이름으로 인증정보(UsernamePasswordAuthenticationToken) 저장
            // headerAccessor.setUser 로 Security 에 저장. WebSocketSecurityConfig 의 권한 검사, accessor.getsUser, SecurityHolder, @AuthenticationPrincipal 모두 사용가능
            headerAccessor.setUser(jwtProvider.getAuthentication(token));

            //확인 로그 찍기
            /*
            Principal principal = headerAccessor.getUser();
            log.info("[INTERCEPTOR] WebSocketInterceptor headerAccessor : getUser {}", principal);
            if(principal instanceof UsernamePasswordAuthenticationToken) {
                UsernamePasswordAuthenticationToken userToken = (UsernamePasswordAuthenticationToken) principal;
                User user = (User) userToken.getPrincipal();
                log.info("[INTERCEPTOR] WebSocketInterceptor headerAccessor : principal to User nickname {}", user.getNickname());
            }
            */
        }

//        log.info("[INTERCEPTOR] END preSend");
        return message;
    }


    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String sessionId = accessor.getSessionId(); //sessionId 얻을 수도 있음

        if(accessor.getCommand() == null){
            log.info("[INTERCEPTOR] postSend command null");
            return;
        }

        // stomp command 값에 따라 작업 처리 가능
        switch (accessor.getCommand()) {
            case CONNECT:
                // 유저가 웹소켓 connect() 한 뒤 호출됨
                log.info("[INTERCEPTOR] postSend command CONNECT. sessionId {}", sessionId);
                break;
            case DISCONNECT:
                // 유저가 웹소켓 disconnect() 한 뒤 or 세션이 끊어졌을 때 호출됨
                log.info("[INTERCEPTOR] postSend command DISCONNECT. sessionId {}", sessionId);

                // 입장했던 유저면 퇴장 로직 진행
                User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                try {
                    stageSocketService.deleteStageUser(user);
                }catch (StageException stageException) {
                    if(stageException.getCode() != StageStatusCode.NOT_ENTERED_USER){
                        throw stageException;
                    }
                }

                break;
            default:
                log.info("[INTERCEPTOR] postSend command {}. sessionId {}", accessor.getCommand(), sessionId);
                break;
        }

    }
}
