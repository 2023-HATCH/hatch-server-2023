package hatch.hatchserver2023.domain.stage.application;

import hatch.hatchserver2023.domain.stage.dto.StageRequestDto;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.code.StageStatusCode;
import hatch.hatchserver2023.global.common.response.exception.StageException;
import hatch.hatchserver2023.global.config.redis.RedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class StageSocketService {
    private final StageDataUtil stageDataUtil;

    private final RedisDao redisDao;

    public StageSocketService(StageDataUtil stageDataUtil, RedisDao redisDao) {
        this.stageDataUtil = stageDataUtil;
        this.redisDao = redisDao;
    }


    public void savePlaySkeleton(StageRequestDto.SendPlaySkeleton dto) {
        log.info("[SERVICE] savePlaySkeleton");

        String status = stageDataUtil.getStageStatus();
        if(status==null || !status.equals(StageRoutineService.STAGE_STATUS_PLAY)) {
            throw new StageException(StageStatusCode.STAGE_STATUS_NOT_PLAY);
        }

        String setName = StageDataUtil.KEY_STAGE_PLAYER_SKELETONS_PREFIX + dto.getPlayerNum();
        String floatArrayString = Arrays.toString(dto.getSkeleton().toAIFloatArray());
//        log.info("savePlaySkeleton floatArrayString : {}", floatArrayString);
        log.info("savePlaySkeleton save frameNum : {}", dto.getFrameNum());
        redisDao.setValuesZSet(setName, floatArrayString, dto.getFrameNum());

    }

    public void checkStageStatusMvp() {
        String status = stageDataUtil.getStageStatus();
        if(status==null || !status.equals(StageRoutineService.STAGE_STATUS_MVP)) {
            throw new StageException(StageStatusCode.STAGE_STATUS_NOT_MVP);
        }
    }

    /**
     * 스테이지 퇴장 로직 (임시) -> http에서 ws로 변경. 프론트 수정 후 삭제 예정
     * @param user
     */
    public int deleteStageUser(User user) throws StageException { // TODO : 8/9 이후 temp로직들 수정
        log.info("[SERVICE] deleteStageUser");

        // 이 유저가 입장되어있는 유저인지 확인 // TODO : Refactor
        boolean isStageUser = stageDataUtil.isStageExistUser(user.getId());
        if(!isStageUser) {
            log.info("[SERVICE] Not stage entered user. No delete");
            throw new StageException(StageStatusCode.NOT_ENTERED_USER);
        }

        int count = stageDataUtil.getStageEnterUserCount();
        log.info("[SERVICE] count : {}", count);

        if(count == 0){
            throw new StageException(StageStatusCode.STAGE_ALREADY_EMPTY);
        }

        int decreasedCount = deleteStageData(user, count);

        tempCheckStageEmpty();

        // 퇴장 요청을 ws로 옮기면서 responser 불필요해짐. 대신 decreaseCount 반환
//        stageSocketResponser.userCount(decreasedCount);
        return decreasedCount;
    }

    private int deleteStageData(User user, int count) {
        // 인원수 decrease
        int decreasedCount = count -1;
        stageDataUtil.setStageEnterUserCount(decreasedCount);
        log.info("[SERVICE] decreasedCount : {}", decreasedCount);

        // redis 입장 목록에서 입장한 사용자 PK 제거
        stageDataUtil.deleteStageEnterUserSet(user.getId());
        return decreasedCount;
    }

    /**
     * 개발편의상 개발한 임시 로직 (한 사용자 여러번 count+1 가능한 환경 유지)
     * 스테이지 exit 시 사용자 목록이 비어있으면 사용자수 0으로 변경
     */
    private void tempCheckStageEmpty() {
        Long size = stageDataUtil.getStageEnterUserSetSize();
        log.info("tempCheckStageEmpty STAGE_ENTER_USER_LIST set size : {}", size);
        if(size==0) {
            log.info("tempCheckStageEmpty set STAGE_ENTER_USER_COUNT = 0");
            stageDataUtil.setStageEnterUserCount(0);
        }
    }

}
