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

        private Double similarity;

        public static StageResponseDto.GetSimilarity toDto(Double similarity) {
            return GetSimilarity.builder()
                    .similarity(similarity)
                    .build();
        }
    }
}
