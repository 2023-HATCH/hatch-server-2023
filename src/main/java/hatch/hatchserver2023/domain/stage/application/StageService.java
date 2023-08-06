package hatch.hatchserver2023.domain.stage.application;

import hatch.hatchserver2023.domain.stage.api.StageSocketResponser;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.code.StageStatusCode;
import hatch.hatchserver2023.global.common.response.exception.StageException;
import hatch.hatchserver2023.global.config.redis.RedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StageService {

    private final StageRoutineService stageRoutineService;

    private final RedisDao redisDao;

    private final StageSocketResponser stageSocketResponser;

    public StageService(RedisDao redisDao, StageRoutineService stageRoutineService, StageSocketResponser stageSocketResponser) {
        this.redisDao = redisDao;
        this.stageRoutineService = stageRoutineService;
        this.stageSocketResponser = stageSocketResponser;
    }


    // TODO : 8/9 이후 주석코드로 변경할 것
    /**
     * 스테이지 입장 로직
     * @param user
     * @return
     */
    public int addStageUser(User user) {
        log.info("[SERVICE] addAndGetStageUserCount");

//        if(isExistUser(user)){
//            throw new StageException(StageStatusCode.ALREADY_ENTERED_USER);
//        }

        int increasedCount = addStageData(user);

        stageSocketResponser.userCount(increasedCount);

        runStageRoutine(increasedCount);

        return increasedCount;
    }

    private int addStageData(User user) {
        // 인원수 increase
        int increasedCount = stageRoutineService.getStageUserCount() + 1;
        redisDao.setValues(StageRoutineService.KEY_STAGE_ENTER_USER_COUNT, String.valueOf(increasedCount));
        log.info("[SERVICE] increasedCount : {}", increasedCount);

        // redis 입장 목록에 입장한 사용자 PK 추가
        redisDao.setValuesSet(StageRoutineService.KEY_STAGE_ENTER_USER_LIST, user.getId().toString());
        return increasedCount;
    }

    /**
     * 스테이지 입장 로직 2 : 중복 입장 불가. 8/9 이후 이걸로 변경
     * @param user
     * @return
     */
//    public int addStageUser(User user) {
//        log.info("[SERVICE] addAndGetStageUserCount");
//
//        int userCount = stageRoutineService.getStageUserCount();
//
//        if(isExistUser(user)){
////            throw new StageException(StageStatusCode.ALREADY_ENTERED_USER);
//            log.info("already entered user in stage");
//        }
//        else{
//            userCount += 1;
//            redisDao.setValuesSet(StageRoutineService.STAGE_ENTER_USER_LIST, user.getId().toString());
//            redisDao.setValues(StageRoutineService.STAGE_ENTER_USER_COUNT, String.valueOf(userCount));
//            runStageRoutine(userCount);
//        }
//
//        stageSocketResponser.userCount(userCount);
//
//        return userCount;
//    }

    // TODO : 스테이지가 진행 도중 멈춰버렸을 때 (왜?) 새로 누군가 입장 시 스테이지 처음부터 새로 run시키기.. how? 상태값이 아니면 진행중(Thread.sleep)인지 아닌지 어떻게 알지... 스레드..?
    private void runStageRoutine(int increasedCount) {
        String stageStatus = getStageStatus();
        switch (stageStatus) {
            case StageRoutineService.STAGE_STATUS_WAIT:
                log.info("stage status : wait ");
                if (increasedCount >= 3) {
                    log.info("stage user count >= 3");
                    stageRoutineService.startRoutine();
                }
                break;

            case StageRoutineService.STAGE_STATUS_CATCH:
                log.info("stage status : catch ");
                break;

            case StageRoutineService.STAGE_STATUS_MVP:
                log.info("stage status : mvp ");
                break;
        }
    }

    private boolean isExistUser(User user) {
        return redisDao.isSetDataExist(StageRoutineService.KEY_STAGE_ENTER_USER_LIST, user.getId().toString());
    }

    /**
     * 스테이지 상태 확인 로직
     * @return
     */
    public String getStageStatus() {
        log.info("[SERVICE] getStageStatus");
        String stageStatus = stageRoutineService.getStageStatus();
        return (stageStatus==null) ? StageRoutineService.STAGE_STATUS_WAIT : stageStatus;
        //TODO : 상태에 따라 진행중인 정보 같이 보내줘야 함
    }

    /**
     * 스테이지 참여자 고유값 목록 확인 로직
     * @return
     */
    public List<Long> getStageEnterUserIds() {
        log.info("[SERVICE] getStageEnterUserProfiles");
        Set<String> userIdSet = redisDao.getValuesSet(StageRoutineService.KEY_STAGE_ENTER_USER_LIST);
        List<String> userIds = new ArrayList<>(userIdSet);
        return userIds.stream().map(Long::parseLong).collect(Collectors.toList());
    }


    /**
     * 스테이지 캐치 등록 로직
     * @param user
     */
    public void registerCatch(User user) {
        log.info("[SERVICE] registerCatch");

        if(!stageRoutineService.getStageStatus().equals(StageRoutineService.STAGE_STATUS_CATCH)) {
            throw new StageException(StageStatusCode.STAGE_STATUS_NOT_CATCH);
        }

        final long now = System.currentTimeMillis();
        log.info("registerCatch user id : {}, nickname : {}, time : {}", user.getId(), user.getNickname(), now);
        redisDao.setValuesZSet(StageRoutineService.KEY_STAGE_CATCH_USER_LIST, user.getId().toString(), (int) now);
//        redisDao.setValuesHash(StageRoutineService.STAGE_CATCH_USER_LIST, (int) now, user);
    }

    /**
     * 스테이지 퇴장 로직 (임시)
     * @param user
     */
    public void deleteStageUser(User user) {
        log.info("[SERVICE] deleteStageUser");

        int count = stageRoutineService.getStageUserCount();
        log.info("[SERVICE] count : {}", count);

        if(count == 0){
            throw new StageException(StageStatusCode.STAGE_ALREADY_EMPTY);
        }

        int decreasedCount = deleteStageData(user, count);

        tempCheckStageEmpty();

        stageSocketResponser.userCount(decreasedCount);
    }

    private int deleteStageData(User user, int count) {
        // 인원수 decrease
        int decreasedCount = count -1;
        redisDao.setValues(StageRoutineService.KEY_STAGE_ENTER_USER_COUNT, String.valueOf(decreasedCount));
        log.info("[SERVICE] decreasedCount : {}", decreasedCount);

        // redis 입장 목록에서 입장한 사용자 PK 제거
        redisDao.removeValuesSet(StageRoutineService.KEY_STAGE_ENTER_USER_LIST, user.getId().toString());
        return decreasedCount;
    }

    /**
     * 개발편의상 개발한 임시 로직 (한 사용자 여러번 count+1 가능한 환경 유지)
     * 스테이지 exit 시 사용자 목록이 비어있으면 사용자수 0으로 변경
     */
    private void tempCheckStageEmpty() {
        Long size = redisDao.getSetSize(StageRoutineService.KEY_STAGE_ENTER_USER_LIST);
        log.info("tempCheckStageEmpty STAGE_ENTER_USER_LIST set size : {}", size);
        if(size==0) {
            log.info("tempCheckStageEmpty set STAGE_ENTER_USER_COUNT = 0");
            redisDao.setValues(StageRoutineService.KEY_STAGE_ENTER_USER_COUNT, "0");
        }
    }

}
