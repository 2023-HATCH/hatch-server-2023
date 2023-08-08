package hatch.hatchserver2023.domain.stage.application;

import hatch.hatchserver2023.global.config.redis.RedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class StageDataService { //public 이 상수KEY는 다른 곳에서 한번씩 쓰여서 메서드화해도 이점이 별로 없음
    public static final String KEY_STAGE_STATUS = "STAGE_STATUS";
    private static final String KEY_STAGE_STATUS_START_TIME = "STAGE_STATUS_START_TIME"; // 스테이지 각 단계의 시작 시각 nanoTime을 저장하는 키
    private static final String KEY_STAGE_ENTER_USER_COUNT = "STAGE_ENTER_USER_COUNT";
    private static final String KEY_STAGE_ENTER_USER_LIST = "STAGE_ENTER_USER_LIST";

    public static final String KEY_STAGE_CATCH_USER_LIST = "STAGE_CATCH_USER_LIST";

    public static final String KEY_STAGE_PLAYER_INFO_HASH = "STAGE_PLAYER_INFO_HASH";
    public static final String KEY_STAGE_PLAYER_SKELETONS_PREFIX = "STAGE_PLAYER_SKELETONS_PREFIX";

    private final RedisDao redisDao;

    public StageDataService(RedisDao redisDao) {
        this.redisDao = redisDao;
    }

    /**
     * 스테이지 상태 저장 메서드
     * @param status
     */
    public void setStageStatus(String status) {
        redisDao.setValues(KEY_STAGE_STATUS, status);
    }

    /**
     * 스테이지 상태 조회 메서드
     * @return
     */
    public String getStageStatus() {
        String stageStatus = redisDao.getValues(KEY_STAGE_STATUS);
        return (stageStatus==null) ? StageRoutineService.STAGE_STATUS_WAIT : stageStatus;
    }

    /**
     * 스테이지 상태 시작 시각 저장 메서드
     * @param
     */
    public void setStageStatusStartTime() { //long time
        redisDao.setValues(KEY_STAGE_STATUS_START_TIME, String.valueOf(System.nanoTime())); //String.valueOf(time)
    }

    /**
     * 스테이지 상태 시작 시각 조회 메서드
     * @return
     */
    public long getStageStatusStartTime() {
        String startTimeString = redisDao.getValues(KEY_STAGE_STATUS_START_TIME);
        startTimeString = (startTimeString==null) ? "0" : startTimeString;
        return Long.parseLong(startTimeString);
    }

    /**
     * 스테이지 입장 인원수 저장 메서드
     * @param userCount
     */
    public void setStageEnterUserCount(int userCount) {
        redisDao.setValues(KEY_STAGE_ENTER_USER_COUNT, String.valueOf(userCount));
    }

    /**
     * 스테이지 입장 인원수 조회 메서드
     * @return
     */
    public int getStageEnterUserCount() {
        String countString = redisDao.getValues(KEY_STAGE_ENTER_USER_COUNT);
        log.info("StageRoutineUtil countString : {}", countString);
        return (countString==null) ? 0 : Integer.parseInt(countString);
    }

    /**
     * 스테이지 사용자 목록에 사용자 PK 추가
     * @param id
     */
    public void addStageEnterUserSet(long id) {
        redisDao.setValuesSet(KEY_STAGE_ENTER_USER_LIST, String.valueOf(id));
    }

    /**
     * 스테이지 입장자 목록 조회 메서드
     * @return
     */
    public Set<String> getStageEnterUsers() {
        return redisDao.getValuesSet(KEY_STAGE_ENTER_USER_LIST);
    }

    /**
     * 스테이지에 입장한 사용자인지 입장자 목록을 확인하는 메서드
     * @param id
     * @return
     */
    public boolean isStageExistUser(long id) {
        return redisDao.isSetDataExist(KEY_STAGE_ENTER_USER_LIST, String.valueOf(id));
    }

    /**
     * (중복 입장 해결을 위한 임시 로직) 스테이지 입장자 목록 크기 조회 메서드
     * @return
     */
    public Long getStageEnterUserSetSize() {
        return redisDao.getSetSize(KEY_STAGE_ENTER_USER_LIST);
    }

    /**
     * 스테이지 입장자 목록에서 유저 삭제 메서드
     * @param id
     */
    public void deleteStageEnterUserSet(long id) {
        redisDao.removeValuesSet(KEY_STAGE_ENTER_USER_LIST, String.valueOf(id));
    }
}
