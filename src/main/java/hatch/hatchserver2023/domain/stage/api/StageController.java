package hatch.hatchserver2023.domain.stage.api;

import hatch.hatchserver2023.domain.stage.application.StageService;
import hatch.hatchserver2023.domain.stage.application.StageSocketService;
import hatch.hatchserver2023.domain.stage.dto.StageResponseDto;
import hatch.hatchserver2023.domain.talk.application.TalkService;
import hatch.hatchserver2023.domain.talk.domain.TalkMessage;
import hatch.hatchserver2023.domain.user.application.UserUtilService;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.StageStatusCode;
import hatch.hatchserver2023.global.common.response.exception.StageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("api/v1/stage")
public class StageController {

    private final StageService stageService;
    private final StageSocketService stageSocketService; //퇴장 api 삭제 후 삭제
    private final TalkService talkService;
    private final UserUtilService userUtilService;

    public StageController(StageService stageService, StageSocketService stageSocketService, TalkService talkService, UserUtilService userUtilService) {
        this.stageService = stageService;
        this.stageSocketService = stageSocketService;
        this.talkService = talkService;
        this.userUtilService = userUtilService;
    }

    // TODO : WAIT 말고 PLAY, MVP 상태에서 입장한 상황 처리로직 추가하기 (음원 정보, 플레이어 정보 등 응답)
    /**
     * 스테이지 입장 api (WAIT 상태에서 입장하는 상황만 가정함)
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
        String stageStatus = stageService.getStageInfo();
        Slice<TalkMessage> talkMessages = talkService.getTalkMessages(page, size);

        // ws upgrade -> x 그냥 다른 요청으로 분리 사용

        return ResponseEntity.ok().body(CommonResponse.toResponse(
                StageStatusCode.GET_STAGE_ENTER_SUCCESS, StageResponseDto.Enter.toDto(stageStatus, stageUserCount, talkMessages)));
    }

    /**
     * 스테이지 사용자 목록 조회 api
     * @return
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping("/users")
    public ResponseEntity<CommonResponse> getStageUsers() {
        log.info("[API] GET /stage/users");
        List<Long> userIds = stageService.getStageEnterUserIds();
        List<User> users = userUtilService.getUsersById(userIds);
        return ResponseEntity.ok().body(CommonResponse.toResponse(
                StageStatusCode.GET_STAGE_ENTER_USER_LIST_SUCCESS, UserResponseDto.SimpleUserProfile.toDtos(users)));
    }

    /**
     * 스테이지 캐치 등록 api
     * @return
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping("/catch")
    public ResponseEntity<CommonResponse> registerCatch(@AuthenticationPrincipal User user) {
        log.info("[API] GET /stage/catch");
        stageService.registerCatch(user);
        return ResponseEntity.ok().body(CommonResponse.toResponse(
                StageStatusCode.GET_CATCH_SUCCESS));
    }

    /**
     * 스테이지 사용자 퇴장 api (임시) -> 삭제 예정
     * @param user
     * @return
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping("/exit")
    public ResponseEntity<CommonResponse> exitStage(@AuthenticationPrincipal User user) {
        log.info("[API] GET /stage/exit");
//        stageService.deleteStageUser(user);

        try {
            stageSocketService.deleteStageUser(user); //TODO : 이 api 삭제
        }catch (StageException stageException) {
            if(stageException.getCode() != StageStatusCode.NOT_ENTERED_USER){
                throw stageException;
            }
        }

        return ResponseEntity.ok().body(CommonResponse.toResponse(
                StageStatusCode.GET_STAGE_EXIT_SUCCESS));
    }


// 이거 두개 필요 없게 됨 - 플레이 스켈레톤 전송 시 스켈레톤 전장해두고 사용하므로

//    /**
//     * 플레이 종료 api
//     *
//     * @param requestDto
//     * @return similarity
//     */
//    @PostMapping("/play/end")
//    public ResponseEntity<Object> endPlay(@RequestBody @Valid StageRequestDto.EndPlay requestDto, @AuthenticationPrincipal @NotNull User user) {
//        log.info("[API] POST /stage/play/end");
//
//        Float similarity = stageService.calculateSimilarity(requestDto.getTitle(), requestDto.getSkeletons());
//        // TODO : redis 에 저장
//
//        return ResponseEntity.ok(CommonResponse.toResponse(CommonCode.OK));
//    }

//    /**
//     * 안무 정확도 계산 API
//     * 음악 제목과 안무 스켈레톤 배열을 입력하면 AI 서버와 통신하여 해당 곡 안무 정답과의 유사도 계산
//     *
//     * @param request
//     * @return similarity
//     */
//    @PostMapping("/similarity")
//    public ResponseEntity<Object> calculateSimilarity(@RequestBody @Valid SimilarityRequestDto request) {
//
//        Float similarity = stageService.calculateSimilarity(request.getTitle(), request.getSkeletons());
//
//        return ResponseEntity.ok(CommonResponse.toResponse(CommonCode.OK, StageResponseDto.GetSimilarity.toDto(similarity)));
//    }
}
