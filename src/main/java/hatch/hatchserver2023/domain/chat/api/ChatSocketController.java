package hatch.hatchserver2023.domain.chat.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class ChatSocketController {
    private final String CHAT_WS_SEND_URL_PREFIX = "/topic/chat-room/"; // + chatRoomId


}
