package hatch.hatchserver2023.domain.stage.api;

import hatch.hatchserver2023.domain.stage.application.StageService;
import hatch.hatchserver2023.domain.stage.dto.SimilarityRequestDto;
import hatch.hatchserver2023.domain.stage.dto.StageResponseDto;
import hatch.hatchserver2023.domain.talk.application.TalkService;
import hatch.hatchserver2023.domain.talk.domain.TalkMessage;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.CommonCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/stage")
public class StageController {

    private final StageService stageService;
    private final TalkService talkService;

    public StageController(StageService stageService, TalkService talkService) {
        this.stageService = stageService;
        this.talkService = talkService;
    }

    /**
     * 스테이지 입장 api
     * @param page
     * @param size
     * @param user
     * @return
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping("/enter")
    public ResponseEntity<CommonResponse> enterStage(@RequestParam @NotNull @Min(0) Integer page,
                                                     @RequestParam @NotNull Integer size,
                                                     @AuthenticationPrincipal User user) {
        log.info("[API] GET /stage/enter");
        int stageUserCount = stageService.addStageUser(user);
        String stageStatus = stageService.getStageStatus();
        Slice<TalkMessage> talkMessages = talkService.getTalkMessages(page, size);

        // ws upgrade

        return ResponseEntity.ok().body(CommonResponse.toResponse(
                CommonCode.OK, StageResponseDto.Enter.toDto(stageStatus, stageUserCount, talkMessages)));
    }


    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping("/users")
    public ResponseEntity<CommonResponse> getStageUsers() {
        log.info("[API] GET /stage/users");
        List<String> users = stageService.getStageEnterUsers(); //TODO : String 말고 User로

        return ResponseEntity.ok().body(CommonResponse.toResponse(
                CommonCode.OK, users));
    }

    /**
     * 안무 정확도 계산 API
     * 음악 제목과 안무 스켈레톤 배열을 입력하면 AI 서버와 통신하여 해당 곡 안무 정답과의 유사도 계산
     *
     * @param request
     * @return similarity
     */
    @PostMapping("/similarity")
    public ResponseEntity<Object> calculateSimilarity(@RequestBody SimilarityRequestDto request) {

        Float similarity = stageService.calculateSimilarity(request.getTitle(), request.getKeypoints());

        return ResponseEntity.ok(CommonResponse.toResponse(CommonCode.OK, StageResponseDto.GetSimilarity.toDto(similarity)));
    }
}
