package hatch.hatchserver2023.domain.stage.application;

import hatch.hatchserver2023.domain.stage.application.StageRoutineService;
import hatch.hatchserver2023.global.config.redis.RedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StageDataService {
    private final RedisDao redisDao;

    public StageDataService(RedisDao redisDao) {
        this.redisDao = redisDao;
    }

    public String getStageStatus() {
        return redisDao.getValues(StageRoutineService.KEY_STAGE_STATUS);
    }

    public void setStageStatus(String status) {
        redisDao.setValues(StageRoutineService.KEY_STAGE_STATUS, status);
    }

    public int getStageUserCount() {
        String countString = redisDao.getValues(StageRoutineService.KEY_STAGE_ENTER_USER_COUNT);
        log.info("StageRoutineUtil countString : {}", countString);
        return (countString==null) ? 0 : Integer.parseInt(countString);
    }
}
