package hatch.hatchserver2023.domain.talk.api;

import hatch.hatchserver2023.domain.talk.application.TalkService;
import hatch.hatchserver2023.domain.talk.domain.TalkMessage;
import hatch.hatchserver2023.domain.talk.dto.TalkRequestDto;
import hatch.hatchserver2023.domain.talk.dto.TalkResponseDto;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Slf4j
@Controller
public class TalkController {
    private final String TYPE_TALK_MESSAGE = "message";
    private final String TYPE_TALK_REACTION = "reaction";

    private final TalkService talkService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public TalkController(TalkService talkService, SimpMessagingTemplate simpMessagingTemplate) {
        this.talkService = talkService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    /**
     * 스테이지 라이브톡 메세지 전송 메서드
     * @param requestDto
     * @param sender : 메세지 전송자
     */
    @MessageMapping("/talks/messages")
    public void sendTalkMessage(TalkRequestDto.SendMessage requestDto, @AuthenticationPrincipal User sender) { //Message message,
        log.info("[WS] /app/talks/messages");
        //주석 로그
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
                .type(TYPE_TALK_MESSAGE)
                .content(savedTalkMessage.getContent())
                .createdAt(savedTalkMessage.getCreatedTime().toString())
                .sender(UserResponseDto.SimpleUserProfile.toDto(sender))
                .build();

        simpMessagingTemplate.convertAndSend("/topic/stage", responseDto);
    }

    /**
     * 스테이지 라이브반응 전송 메서드
     */
    @MessageMapping("/talks/reactions")
    public void sendTalkReaction() {
        TalkResponseDto.SendReaction responseDto = TalkResponseDto.SendReaction.builder()
                .type(TYPE_TALK_REACTION)
                .createdAt(LocalDateTime.now().toString())
                .build();

        simpMessagingTemplate.convertAndSend("/topic/stage", responseDto);
    }

}