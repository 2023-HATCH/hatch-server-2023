package hatch.hatchserver2023.domain.stage.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

public class AIModel {

    @ToString
    @Getter
    @Builder
    public static class SimilarityCalculateInfo {
        private Float similarity;
        private int usedAnswerFrameCount;

        public static SimilarityCalculateInfo toDto(Float similarity, int usedAnswerFrameCount) {
            return SimilarityCalculateInfo.builder()
                    .similarity(similarity)
                    .usedAnswerFrameCount(usedAnswerFrameCount)
                    .build();
        }
    }

}
