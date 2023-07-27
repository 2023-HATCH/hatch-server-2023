package hatch.hatchserver2023.domain.talk.api;

import hatch.hatchserver2023.domain.talk.application.TalkService;
import hatch.hatchserver2023.domain.talk.domain.TalkMessage;
import hatch.hatchserver2023.domain.talk.dto.TalkRequestDto;
import hatch.hatchserver2023.domain.talk.dto.TalkResponseDto;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.socket.SocketResponseType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Slf4j
@Controller
public class TalkWebSocketController {
    private final String TALK_WS_SEND_URL = "/topic/stage";

    private final TalkService talkService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public TalkWebSocketController(TalkService talkService, SimpMessagingTemplate simpMessagingTemplate) {
        this.talkService = talkService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    /**
     * 스테이지 라이브톡 메세지 전송 api (ws)
     * @param requestDto
     * @param sender : 메세지 전송자
     */
    @MessageMapping("/talks/messages")
//    @PreAuthorize("hasAnyRole('ROLE_USER')") //어차피 securityConfig 단에서부터 막혀서 여기다 설정할 필요 없음
    public void sendTalkMessage(@Valid TalkRequestDto.SendMessage requestDto, @AuthenticationPrincipal @NotNull User sender) { //Message message,
        log.info("[WS] /app/talks/messages");
        //확인용 로그들 주석
/*        log.info("[WS] message : {}", message);
        log.info("requestDto : {}", requestDto);

        //유저 인증정보 받아오는 거 setUser만 하면 getUser(simpUser), @AuthenticationPrincipal, SecurityContextHolder 셋 다 잘 됨!
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        UsernamePasswordAuthenticationToken userToken = (UsernamePasswordAuthenticationToken) accessor.getUser();
        log.info("[WS] sendTalkMessage userToken : {}", userToken);
        User simpUser = (User) userToken.getPrincipal();
        log.info("simpUser nickname : {}", simpUser.getNickname());
        log.info("SecurityContextHolder principal : {}", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
*/
        log.info("@AuthenticationPrincipal user : {}", sender.getNickname());

        // DB 저장
        TalkMessage savedTalkMessage = talkService.saveTalkMessage(requestDto.toEntity(), sender);

        TalkResponseDto.SendMessage responseDto = TalkResponseDto.SendMessage.builder()
                .content(savedTalkMessage.getContent())
                .sender(UserResponseDto.SimpleUserProfile.toDto(sender))
                .build();

        simpMessagingTemplate.convertAndSend(TALK_WS_SEND_URL, CommonResponse.toSocketResponse(SocketResponseType.TALK_MESSAGE, responseDto));
    }

    /**
     * 스테이지 라이브반응 전송 api (ws)
     */
    @MessageMapping("/talks/reactions")
    public void sendTalkReaction() {
        log.info("[WS] /app/talks/reactions");
        simpMessagingTemplate.convertAndSend(TALK_WS_SEND_URL, CommonResponse.toSocketResponse(SocketResponseType.TALK_REACTION));
    }

}
