package hatch.hatchserver2023.domain.stage.api;

import hatch.hatchserver2023.domain.stage.application.StageService;
import hatch.hatchserver2023.domain.stage.dto.SimilarityRequestDto;
import hatch.hatchserver2023.domain.stage.dto.StageResponseDto;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.CommonCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/v1/stages")
@RequiredArgsConstructor
public class StageController {

    private final StageService stageService;

    /**
     * 안무 정확도 계산 API
     * 음악 제목과 안무 스켈레톤 배열을 입력하면 AI 서버와 통신하여 해당 곡 안무 정답과의 유사도 계산
     *
     * @param request
     * @return similarity
     */
    @PostMapping("/similarity")
    public ResponseEntity<Object> calculateSimilarity(@RequestBody SimilarityRequestDto request) {

        Double similarity = stageService.calculateSimilarity(request.getTitle(), request.getKeypoints());

        return ResponseEntity.ok(CommonResponse.toResponse(CommonCode.OK, StageResponseDto.GetSimilarity.toDto(similarity)));
    }
}
