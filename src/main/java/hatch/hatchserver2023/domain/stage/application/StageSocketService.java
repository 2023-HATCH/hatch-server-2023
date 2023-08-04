package hatch.hatchserver2023.domain.stage.application;

import hatch.hatchserver2023.domain.stage.dto.StageRequestDto;
import hatch.hatchserver2023.global.common.response.code.StageStatusCode;
import hatch.hatchserver2023.global.common.response.exception.StageException;
import hatch.hatchserver2023.global.config.redis.RedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StageSocketService {
    private final StageRoutineService stageRoutineService;

    private final RedisDao redisDao;

    public StageSocketService(StageRoutineService stageRoutineService, RedisDao redisDao) {
        this.stageRoutineService = stageRoutineService;
        this.redisDao = redisDao;
    }


    public void savePlaySkeleton(StageRequestDto.SendPlaySkeleton dto) {
        String status = stageRoutineService.getStageStatus();
        if(status==null || !status.equals(StageRoutineService.STAGE_STATUS_PLAY)) {
            throw new StageException(StageStatusCode.STAGE_STATUS_NOT_PLAY);
        }

//        String hashName = StageRoutineService.STAGE_PLAY_SKELETONS_PREFIX+dto.getPlayerNum();
//        redisDao.setValuesHash(hashName, dto.getFrameNum().toString(), dto.getSkeleton());
    }
}
