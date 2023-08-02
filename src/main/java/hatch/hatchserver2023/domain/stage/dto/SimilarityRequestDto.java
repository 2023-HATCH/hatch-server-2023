package hatch.hatchserver2023.domain.stage.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

// Front -> API Server
@ToString
@Getter
@Builder
public class SimilarityRequestDto {

    // 음악 제목
    @NotNull
    private String title;

    // 안무 관절 데이터
    @NotNull
    private List<StageRequestDto.Skeleton> skeletons;

//    private Float[][] keypoints;
}
