package hatch.hatchserver2023.domain.stage.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hatch.hatchserver2023.domain.stage.dto.StageRequestDto;
import hatch.hatchserver2023.global.common.response.code.StageStatusCode;
import hatch.hatchserver2023.global.common.response.exception.StageException;
import hatch.hatchserver2023.global.config.redis.RedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
        log.info("[SERVICE] savePlaySkeleton");

        String status = stageRoutineService.getStageStatus();
        if(status==null || !status.equals(StageRoutineService.STAGE_STATUS_PLAY)) {
            throw new StageException(StageStatusCode.STAGE_STATUS_NOT_PLAY);
        }

        ///ZSet
        String setName = StageRoutineService.KEY_STAGE_PLAYER_SKELETONS_PREFIX +dto.getPlayerNum();
        String floatArrayString = Arrays.toString(dto.getSkeleton().toAIFloatArray());
//        log.info("savePlaySkeleton floatArrayString : {}", floatArrayString);
        redisDao.setValuesZSet(setName, floatArrayString, dto.getFrameNum());



        ///test
        Set<String> dataSet = redisDao.getValuesZSetAll(setName);
        log.info("savePlaySkeleton dataSet : {}", dataSet);
        String arrayString = (String) dataSet.toArray()[0];
        log.info("savePlaySkeleton dataSet.toArray()[0] : {}", arrayString);
//        Float[] floatArray = (Float[]) dataSet.toArray()[0];
        List floatArray = null;
        try {
            floatArray = new ObjectMapper().readValue(arrayString, List.class);
        } catch (JsonProcessingException e) {
            log.info("savePlaySkeleton floatArray ObjectMapper error");
            throw new RuntimeException(e);
        }
        log.info("savePlaySkeleton floatArray : {}", floatArray);
        log.info("savePlaySkeleton floatArray.get(0) : {}", floatArray.get(0));

        //Hash
//        String hashName = StageRoutineService.STAGE_PLAY_SKELETONS_PREFIX+dto.getPlayerNum();
//        redisDao.setValuesHash(hashName, dto.getFrameNum().toString(), dto.getSkeleton());
    }
}
