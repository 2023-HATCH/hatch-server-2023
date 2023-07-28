package hatch.hatchserver2023.domain.stage.api;

import hatch.hatchserver2023.domain.stage.application.StageRoutineService;
import hatch.hatchserver2023.domain.stage.application.StageService;
import hatch.hatchserver2023.domain.stage.dto.StageRequestDto;
import hatch.hatchserver2023.domain.stage.dto.StageSocketResponseDto;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.StageStatusCode;
import hatch.hatchserver2023.global.common.response.exception.StageException;
import hatch.hatchserver2023.global.common.response.socket.SocketResponseType;
import lombok.extern.slf4j.Slf4j;
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

    private final StageService stageService;
    private final StageRoutineService stageRoutineService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public StageSocketController(StageService stageService, StageRoutineService stageRoutineService, SimpMessagingTemplate simpMessagingTemplate) {
        this.stageService = stageService;
        this.stageRoutineService = stageRoutineService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/stage/play/skeleton")
    public void sendPlayerSkeleton(@Valid StageRequestDto.SendPlaySkeleton requestDto, @AuthenticationPrincipal @NotNull User player) {
        log.info("[WS] /stage/play/skeleton");

        if(!stageRoutineService.getStageStatus().equals(StageRoutineService.STAGE_STATUS_PLAY)) {
            throw new StageException(StageStatusCode.STAGE_STATUS_NOT_PLAY);
        }

        StageSocketResponseDto.SendPlaySkeleton responseDto = StageSocketResponseDto.SendPlaySkeleton.toDto(requestDto, player);
        simpMessagingTemplate.convertAndSend(STAGE_WS_SEND_URL, CommonResponse.toSocketResponse(SocketResponseType.PLAY_SKELETON, responseDto));
    }
}
