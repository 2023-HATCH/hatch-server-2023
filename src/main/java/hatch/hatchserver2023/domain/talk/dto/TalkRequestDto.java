package hatch.hatchserver2023.domain.talk.dto;

import hatch.hatchserver2023.domain.talk.domain.TalkMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

public class TalkRequestDto {

    @ToString
    @Getter
    @Builder
    public static class SendMessage{
        private String content;
//        private String tempSender; //TODO : 이건 필요없음. 인증은 토큰으로 할거니까

        public SendMessage() {
        }

        public SendMessage(String content) {
            this.content = content;
        }

        public TalkMessage toEntity() {
            return TalkMessage.builder()
                    .content(this.content)
                    .build();
        }
    }
}
