package hatch.hatchserver2023.domain.stage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
