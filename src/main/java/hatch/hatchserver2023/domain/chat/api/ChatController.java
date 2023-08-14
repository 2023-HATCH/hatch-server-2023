package hatch.hatchserver2023.domain.chat.api;

import hatch.hatchserver2023.domain.chat.application.ChatService;
import hatch.hatchserver2023.domain.chat.domain.ChatMessage;
import hatch.hatchserver2023.domain.chat.domain.ChatRoom;
import hatch.hatchserver2023.domain.chat.dto.ChatModel;
import hatch.hatchserver2023.domain.chat.dto.ChatRequestDto;
import hatch.hatchserver2023.domain.chat.dto.ChatResponseDto;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.ChatStatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
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
    @PostMapping("/rooms")
    public ResponseEntity<CommonResponse> createChatRoom(@Valid ChatRequestDto.CreateChatRoom requestDto,
                                                         @AuthenticationPrincipal @NotNull User user) {
        UUID chatRoomId = chatService.createChatRoom(user, requestDto.getOpponentUserId());
        return ResponseEntity.ok().body(CommonResponse.toResponse(ChatStatusCode.POST_CREATE_CHAT_ROOM_SUCCESS,
                ChatResponseDto.CreateChatRoom.toDto(chatRoomId)));
    }

    /**
     * 채팅방 목록 조회 api
     * @param user
     * @return
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping("/rooms")
    public ResponseEntity<CommonResponse> getChatRooms(@AuthenticationPrincipal @NotNull User user) {
        List<ChatModel.ChatRoomInfo> chatRoomInfos = chatService.getChatRoomInfos(user);
        return ResponseEntity.ok().body(CommonResponse.toResponse(ChatStatusCode.GET_CHAT_ROOMS_SUCCESS,
                ChatResponseDto.GetChatRooms.toDto(chatRoomInfos)));
    }


    /**
     * 채팅 메세지 목록 조회 api
     * @param chatRoomId
     * @param page
     * @param size
     * @return
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<CommonResponse> getChatMessages(@PathVariable @NotNull UUID chatRoomId,
                                                          @RequestParam @NotNull @Min(0) Integer page, @RequestParam @NotNull Integer size) {
        Slice<ChatMessage> chatMessages = chatService.getChatMessages(chatRoomId, page, size);
        return ResponseEntity.ok().body(CommonResponse.toResponse(ChatStatusCode.GET_CHAT_MESSAGES_SUCCESS,
                ChatResponseDto.GetChatMessages.toDto(chatMessages)));
    }
}
