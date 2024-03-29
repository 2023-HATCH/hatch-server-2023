package hatch.hatchserver2023.domain.stage.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import hatch.hatchserver2023.domain.stage.domain.Music;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
import hatch.hatchserver2023.global.common.ObjectMapperUtil;
import hatch.hatchserver2023.global.common.response.code.StageStatusCode;
import hatch.hatchserver2023.global.common.response.exception.StageException;
import hatch.hatchserver2023.global.common.response.socket.StageStatusType;
import hatch.hatchserver2023.global.config.redis.RedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component //@Service @Component? 별 차이는 없다고 함 AOP..? Service는 비느지스 로직을 의미
public class StageDataUtil { //public 이 상수KEY는 다른 곳에서 한번씩 쓰여서 메서드화해도 이점이 별로 없음
    private static final String KEY_PREFIX_STAGE = "stage:";
    public static final String KEY_STAGE_STATUS = KEY_PREFIX_STAGE+"status";
    public static final String KEY_STAGE_MUSIC = KEY_PREFIX_STAGE+"music";
    private static final String KEY_STAGE_STATUS_START_TIME = KEY_PREFIX_STAGE+"status:startTime"; // 스테이지 각 단계의 시작 시각 nanoTime을 저장하는 키
    private static final String KEY_STAGE_ENTER_USER_COUNT = KEY_PREFIX_STAGE+"enterUser:count";
    private static final String KEY_STAGE_ENTER_USER_LIST = KEY_PREFIX_STAGE+"enterUser:info";

    public static final String KEY_STAGE_CATCH_USER_LIST = KEY_PREFIX_STAGE+"catchUser:info";

    public static final String KEY_STAGE_PLAYER_INFO_HASH = KEY_PREFIX_STAGE+"player:info";
    public static final String KEY_STAGE_PLAYER_SKELETON = KEY_PREFIX_STAGE+"player:skeleton:";
    public static final String KEY_STAGE_PLAYER_SKELETON_MID_INDEX = KEY_PREFIX_STAGE+"player:skeleton:midIndex:";

    private final RedisDao redisDao;

    private final ObjectMapperUtil objectMapperUtil;

    public StageDataUtil(RedisDao redisDao, ObjectMapperUtil objectMapperUtil) {
        this.redisDao = redisDao;
        this.objectMapperUtil = objectMapperUtil;
    }

    /**
     * 스테이지 상태 저장 메서드 by String
     * @param status
     */
    public void setStageStatus(String status) {
        redisDao.setValues(KEY_STAGE_STATUS, status);
    }

    /**
     * 스테이지 상태 저장 메서드 by enum
     * @param status
     */
    public void setStageStatus(StageStatusType status) {
        redisDao.setValues(KEY_STAGE_STATUS, status.getType());
    }

    /**
     * 스테이지 상태 조회 메서드
     * @return
     */
    public String getStageStatus() {
        String stageStatus = redisDao.getValues(KEY_STAGE_STATUS);
        return (stageStatus==null) ? StageStatusType.WAIT.getType() : stageStatus;
    }

    /**
     * 스테이지 상태 시작 시각 저장 메서드
     * @param
     */
    public void setStageStatusStartTime() { //long time
        redisDao.setValues(KEY_STAGE_STATUS_START_TIME, String.valueOf(System.nanoTime())); //String.valueOf(time)
    }

    /**
     * 스테이지 상태 시작 시각 조회 메서드. null이면 "0" 반환
     * @return
     */
    public Long getStageStatusStartTime(String status) {
        if(status.equals(StageStatusType.CATCH.getType()) || status.equals(StageStatusType.PLAY.getType()) || status.equals(StageStatusType.MVP.getType())) { // !status.equals(StageRoutineService.STAGE_STATUS_WAIT) || !status.substring(status.length()-3).equals("_END") ??
            String startTimeString = redisDao.getValues(KEY_STAGE_STATUS_START_TIME);
            startTimeString = (startTimeString==null) ? "0" : startTimeString;
            return Long.parseLong(startTimeString);
        }else {
            return null;
        }
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

    /**
     * 플레이어 사용자 정보 저장
     * @param users
     */
    public void savePlayerInfo(List<User> users) {
        List<UserResponseDto.SimpleUserProfile> userSimples = users.stream().map(UserResponseDto.SimpleUserProfile::toDto).collect(Collectors.toList());
        for(int i=0; i<userSimples.size(); i++){ // i는 playerNum과 같음
            String userSimpleJson;
            userSimpleJson = objectMapperUtil.toJson(userSimples.get(i));
            redisDao.setValuesHash(StageDataUtil.KEY_STAGE_PLAYER_INFO_HASH, String.valueOf(i), userSimpleJson);
        }
    }

    /**
     * 플레이 직후 playerNum에 해당하는 플레이어 사용자 정보 가져오기
     * @param playerNum
     * @return
     */
    public UserResponseDto.SimpleUserProfile getPlayerUserInfo(int playerNum) {
        Object userObject = redisDao.getValuesHash(StageDataUtil.KEY_STAGE_PLAYER_INFO_HASH, String.valueOf(playerNum));
        if(userObject==null) {
            throw new StageException(StageStatusCode.FAIL_GET_PLAYER_USER_FROM_REDIS);
        }
        String userJson = userObject.toString();

        UserResponseDto.SimpleUserProfile userInfo;
        try {
            userInfo = objectMapperUtil.toOriginalType(userJson, UserResponseDto.SimpleUserProfile.class);
        } catch (JsonProcessingException e) {
            throw new StageException(StageStatusCode.FAIL_GET_MVP_USER_INFO_FROM_REDIS_JSON);
        }
        return userInfo;
    }

    /**
     * 스테이지 현재 음악 저장 메서드
     * @param music
     */
    public void setStageMusic(Music music) {
        String musicJson = objectMapperUtil.toJson(music);
        redisDao.setValues(KEY_STAGE_MUSIC, musicJson);
    }

    /**
     * 스테이지 현재 음악 조회 메서드
     * @return
     */
    public Music getStageMusic() {
        String musicJson = redisDao.getValues(KEY_STAGE_MUSIC);
        if(musicJson == null){
            return null;
        }

        Music music;
        try{
            music = objectMapperUtil.toOriginalType(musicJson, Music.class);
        } catch (JsonProcessingException e) {
            throw new StageException(StageStatusCode.FAIL_GET_STAGE_MUSIC_FROM_REDIS_JSON);
        }
        return music;
    }



    ///////////////// 개발용 ////////////////////

    /**
     * 개발용 스테이지 데이터 초기화 메서드
     */
    public void initStage() {
        redisDao.deleteValues(StageDataUtil.KEY_STAGE_STATUS); // 스테이지 상태
        redisDao.deleteValues(StageDataUtil.KEY_STAGE_STATUS_START_TIME); // 스테이지 상태

        redisDao.deleteValues(StageDataUtil.KEY_STAGE_ENTER_USER_LIST); // 스테이지 입장자
        redisDao.deleteValues(StageDataUtil.KEY_STAGE_ENTER_USER_COUNT); // 스테이지 입장자 수

        redisDao.deleteValues(StageDataUtil.KEY_STAGE_MUSIC); // 캐치 음악
        redisDao.deleteValues(StageDataUtil.KEY_STAGE_CATCH_USER_LIST); // 캐치 사용자

        //플레이 스켈레톤 데이터 초기화
        for (int i=0; i<=StageRoutineService.STAGE_CATCH_SUCCESS_LAST_INDEX; i++) {
            redisDao.deleteValues(StageDataUtil.KEY_STAGE_PLAYER_SKELETON +i);
            redisDao.deleteValues(StageDataUtil.KEY_STAGE_PLAYER_SKELETON_MID_INDEX +i);
        }
        redisDao.deleteValues(StageDataUtil.KEY_STAGE_PLAYER_INFO_HASH); // 플레이어 데이터
    }
}
