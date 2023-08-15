package hatch.hatchserver2023.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Slf4j
public class ChatRequestDto {

    @ToString
    @Getter
    @Builder
    public static class CreateChatRoom {
        @NotBlank
        private String opponentUserId; // UUID로 하면 validation 어노테이션 적용 시 에러남

        public CreateChatRoom() {
        }

        public CreateChatRoom(String opponentUserId) {
            this.opponentUserId = opponentUserId;
        }
    }

    @ToString
    @Getter
    @Builder
    public static class SendChatMessage {
        @NotNull
        private UUID chatRoomId;

        @Size(max = 200) //min=0 default
        @NotBlank
        private String content;
    }
}
