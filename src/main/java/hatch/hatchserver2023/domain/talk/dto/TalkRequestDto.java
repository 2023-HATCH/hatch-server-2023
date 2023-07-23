package hatch.hatchserver2023.domain.talk.dto;

import hatch.hatchserver2023.domain.talk.domain.TalkMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

public class TalkRequestDto {

    @ToString
    @Getter
    @Builder
    public static class SendMessage{
        @NotBlank
        private String content;

        public TalkMessage toEntity() {
            return TalkMessage.builder()
                    .content(this.content)
                    .build();
        }

        public SendMessage() {
        }

        public SendMessage(String content) {
            this.content = content;
        }
    }
}
