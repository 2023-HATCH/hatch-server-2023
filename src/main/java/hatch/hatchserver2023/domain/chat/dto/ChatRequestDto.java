package hatch.hatchserver2023.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
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
}
