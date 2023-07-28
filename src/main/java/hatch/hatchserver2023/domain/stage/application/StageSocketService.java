package hatch.hatchserver2023.domain.stage.application;

import hatch.hatchserver2023.global.common.response.code.StageStatusCode;
import hatch.hatchserver2023.global.common.response.exception.StageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StageSocketService {
    private final StageRoutineService stageRoutineService;

    public StageSocketService(StageRoutineService stageRoutineService) {
        this.stageRoutineService = stageRoutineService;
    }


    public void sendPlayerSkeleton() {
        if(!stageRoutineService.getStageStatus().equals(StageRoutineService.STAGE_STATUS_PLAY)) {
            throw new StageException(StageStatusCode.STAGE_STATUS_NOT_PLAY);
        }
    }
}
