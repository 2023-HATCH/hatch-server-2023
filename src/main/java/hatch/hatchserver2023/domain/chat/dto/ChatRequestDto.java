package hatch.hatchserver2023.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Slf4j
public class ChatRequestDto {

    @ToString
    @Getter
    @Builder
    public static class CreateChatRoom {
        @NotNull
        private UUID opponentUserId;
    }

    @ToString
    @Getter
    @Builder
    public static class SendChatMessage {
        @NotNull
        private UUID chatRoomId;

        @Size(max = 200) //min=0 default
        @NotNull
        private String content;
    }
}
