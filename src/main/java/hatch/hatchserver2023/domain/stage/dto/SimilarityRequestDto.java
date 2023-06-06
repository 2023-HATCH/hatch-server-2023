package hatch.hatchserver2023.domain.stage.dto;

import lombok.Data;

// Front -> API Server
@Data
public class SimilarityRequestDto {

    // 음악 제목
    private String title;

    // 안무 관절 데이터
    private Double[][] keypoints;
}
