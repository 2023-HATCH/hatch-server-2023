package hatch.hatchserver2023.domain.stage.dto;

import hatch.hatchserver2023.domain.talk.domain.TalkMessage;
import hatch.hatchserver2023.domain.talk.dto.TalkResponseDto;
import lombok.*;
import org.springframework.data.domain.Slice;

public class StageResponseDto {

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetSimilarity {

        private Float similarity;

        public static StageResponseDto.GetSimilarity toDto(Float similarity) {
            return GetSimilarity.builder()
                    .similarity(similarity)
                    .build();
        }
    }


    @ToString
    @Getter
    @Builder
    public static class Enter {
        private String stageStatus;
        private Integer userCount;
        private TalkResponseDto.GetMessagesContainer talkMessageData;

        public static StageResponseDto.Enter toDto(String stageStatus, Integer userCount, Slice<TalkMessage> talkMessages) {
            return Enter.builder()
                    .stageStatus(stageStatus)
                    .userCount(userCount)
                    .talkMessageData(TalkResponseDto.GetMessagesContainer.toDto(talkMessages))
                    .build();
        }
    }
}
