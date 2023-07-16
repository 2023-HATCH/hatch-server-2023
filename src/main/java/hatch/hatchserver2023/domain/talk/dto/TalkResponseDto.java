package hatch.hatchserver2023.domain.talk.dto;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;


public class TalkResponseDto {

    @ToString
    @Getter
    @Builder
    public static class SendMessage{
        private String type;
        private String createdAt;
        private String content;
        private UserResponseDto.SimpleUserProfile sender;
    }

    @ToString
    @Getter
    @Builder
    public static class SendReaction{
        private String type;
        private String createdAt;
    }
}
