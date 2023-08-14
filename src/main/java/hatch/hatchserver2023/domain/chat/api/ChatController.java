package hatch.hatchserver2023.domain.chat.api;

import hatch.hatchserver2023.domain.chat.application.ChatService;
import hatch.hatchserver2023.domain.chat.dto.ChatRequestDto;
import hatch.hatchserver2023.domain.chat.dto.ChatResponseDto;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.ChatStatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping("api/v1/chats")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }


    /**
     * 채딩방 생성 api
     * @param user
     * @param requestDto
     * @return
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @PostMapping("/room")
    public ResponseEntity<CommonResponse> createChatRoom(@AuthenticationPrincipal @NotNull User user,
                                                         @Valid ChatRequestDto.CreateChatRoom requestDto) {
        UUID chatRoomId = chatService.createChatRoom(user, requestDto.getOpponentUserId());
        return ResponseEntity.ok().body(CommonResponse.toResponse(ChatStatusCode.POST_CREATE_CHAT_ROOM_SUCCESS,
                ChatResponseDto.CreateChatRoom.toDto(chatRoomId)));
    }



}
