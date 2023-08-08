package hatch.hatchserver2023.domain.stage.api;

import hatch.hatchserver2023.domain.stage.application.StageRoutineService;
import hatch.hatchserver2023.domain.stage.application.StageSocketService;
import hatch.hatchserver2023.domain.stage.dto.StageRequestDto;
import hatch.hatchserver2023.domain.stage.dto.StageSocketResponseDto;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.StageStatusCode;
import hatch.hatchserver2023.global.common.response.exception.StageException;
import hatch.hatchserver2023.global.common.response.socket.SocketResponseType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Slf4j
@Controller
public class StageSocketController {
    private final String STAGE_WS_SEND_URL = "/topic/stage";

    private final StageSocketService stageSocketService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public StageSocketController(StageSocketService stageSocketService, SimpMessagingTemplate simpMessagingTemplate) {
        this.stageSocketService = stageSocketService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    /**
     * play 스켈레톤 전송 ws
     * @param requestDto
     * @param player
     */
    @MessageMapping("/stage/play/skeleton")
    public void sendPlaySkeleton(@Valid StageRequestDto.SendPlaySkeleton requestDto, @AuthenticationPrincipal @NotNull User player) {
        log.info("[WS] /stage/play/skeleton");

        stageSocketService.savePlaySkeleton(requestDto);

        StageSocketResponseDto.SendPlaySkeleton responseDto = StageSocketResponseDto.SendPlaySkeleton.toDto(requestDto, player);
        simpMessagingTemplate.convertAndSend(STAGE_WS_SEND_URL, CommonResponse.toSocketResponse(SocketResponseType.PLAY_SKELETON, responseDto));
    }

    /**
     * mvp 스켈레톤 전송 ws
     * @param requestDto
     * @param mvp
     */
    @MessageMapping("/stage/mvp/skeleton")
    public void sendMvpSkeleton(@Valid StageRequestDto.SendMvpSkeleton requestDto, @AuthenticationPrincipal @NotNull User mvp) {
        log.info("[WS] /stage/mvp/skeleton");

        stageSocketService.checkStageStatusMvp();

        StageSocketResponseDto.SendMvpSkeleton responseDto = StageSocketResponseDto.SendMvpSkeleton.toDto(requestDto, mvp);
        simpMessagingTemplate.convertAndSend(STAGE_WS_SEND_URL, CommonResponse.toSocketResponse(SocketResponseType.MVP_SKELETON, responseDto));
    }

    /**
     * 퇴장 ws
     * @param user
     */
    @MessageMapping("/stage/exit")
    public void exitStage(@AuthenticationPrincipal @NotNull User user) {
        log.info("[WS] /stage/exit");

        Integer userCount = null;
        try {
            userCount = stageSocketService.deleteStageUser(user);
        }catch (StageException stageException) {
            if(stageException.getCode() == StageStatusCode.NOT_ENTERED_USER){
                log.info(stageException.getCode().getMessage());
                return; // 인원수 변경 응답 없이 종료
            }
        }

        StageSocketResponseDto.UserCount dto = StageSocketResponseDto.UserCount.builder().userCount(userCount).build();
        simpMessagingTemplate.convertAndSend(STAGE_WS_SEND_URL, CommonResponse.toSocketResponse(SocketResponseType.USER_COUNT, dto));
    }
}
