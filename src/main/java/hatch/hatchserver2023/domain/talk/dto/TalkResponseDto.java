package hatch.hatchserver2023.domain.talk.dto;

import hatch.hatchserver2023.domain.talk.domain.TalkMessage;
import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
import hatch.hatchserver2023.global.common.response.code.TalkStatusCode;
import hatch.hatchserver2023.global.common.response.exception.TalkException;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.stream.Collectors;


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

    @ToString
    @Getter
    @Builder
    public static class GetMessagesContainer{
        private Integer page;
        private Integer size;
        private List<BasicMessage> messages;

        public static GetMessagesContainer toDto(Slice<TalkMessage> talkMessages) {
            return GetMessagesContainer.builder()
                    .page(talkMessages.getNumber())
                    .size(talkMessages.getSize())
                    .messages(BasicMessage.toDtos(talkMessages.getContent()))
                    .build();
        }
    }

    @ToString
    @Getter
    @Builder
    public static class BasicMessage{
        private String content;
        private String createdAt;
        private UserResponseDto.SimpleUserProfile sender;

        public static BasicMessage toDto(TalkMessage talkMessage) {
            if(talkMessage.getCreatedTime() == null) {
                throw new TalkException(TalkStatusCode.CREATED_TIME_NULL);
            }

            return BasicMessage.builder()
                    .content(talkMessage.getContent())
                    .createdAt(talkMessage.getCreatedTime().toString())
                    .sender(UserResponseDto.SimpleUserProfile.toDto(talkMessage.getUser()))
                    .build();
        }

        public static List<BasicMessage> toDtos(List<TalkMessage> messages) {
            return messages.stream().map(BasicMessage::toDto).collect(Collectors.toList());
        }
    }
}
