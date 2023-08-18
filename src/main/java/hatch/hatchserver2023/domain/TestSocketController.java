package hatch.hatchserver2023.domain;

import hatch.hatchserver2023.domain.chat.domain.ChatMessage;
import hatch.hatchserver2023.domain.chat.dto.ChatRequestDto;
import hatch.hatchserver2023.domain.chat.dto.ChatResponseDto;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.ChatStatusCode;
import hatch.hatchserver2023.global.common.response.exception.ChatException;
import hatch.hatchserver2023.global.common.response.exception.DefaultException;
import hatch.hatchserver2023.global.common.response.socket.SocketResponseType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.security.Principal;

@Slf4j
@Controller
public class TestSocketController {
//    private final String TEST_WS_SEND_URL = "/topic/test";

//    private final SimpMessagingTemplate simpMessagingTemplate;


    @MessageMapping("/test/exception")
    public void makeException(Principal principal) throws DefaultException {
        log.info("[WS] /test/exception");
        log.info("session principal name : {}", principal.getName());

        throw new ChatException(ChatStatusCode.CHAT_ROOM_UUID_NOT_FOUND); // GlobalExceptionHandler 에서 잡아서 처리됨
    }
}
