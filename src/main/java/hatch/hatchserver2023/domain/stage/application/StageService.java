package hatch.hatchserver2023.domain.stage.application;

import hatch.hatchserver2023.domain.stage.StageModel;
import hatch.hatchserver2023.domain.stage.api.StageSocketResponser;
import hatch.hatchserver2023.domain.stage.domain.Music;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.code.StageStatusCode;
import hatch.hatchserver2023.global.common.response.exception.StageException;
import hatch.hatchserver2023.global.config.redis.RedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
//@Transactional(readOnly = true) // repository가 쓰이는 곳에 없음. 다 redis 임
@Service
public class StageService {

    private final StageRoutineService stageRoutineService;
    private final StageDataUtil stageDataUtil;

    private final RedisDao redisDao;

    private final StageSocketResponser stageSocketResponser;

    public StageService(RedisDao redisDao, StageRoutineService stageRoutineService, StageDataUtil stageDataUtil, StageSocketResponser stageSocketResponser) {
        this.redisDao = redisDao;
        this.stageRoutineService = stageRoutineService;
        this.stageDataUtil = stageDataUtil;
        this.stageSocketResponser = stageSocketResponser;
    }


    /**
     * 스테이지 입장 로직
     * @param user
     * @return
     */
    public int addStageUser(User user) {
        log.info("[SERVICE] addAndGetStageUserCount");

//        if(stageDataService.isStageExistUser(user.getId())){
//            throw new StageException(StageStatusCode.ALREADY_ENTERED_USER);
//        }

        int increasedCount = addStageData(user);

        stageSocketResponser.userCount(increasedCount);

        runStageRoutine(increasedCount);

        return increasedCount;
    }

    private int addStageData(User user) {
        // 인원수 increase
        int increasedCount = stageDataUtil.getStageEnterUserCount() + 1;
        stageDataUtil.setStageEnterUserCount(increasedCount);
        log.info("[SERVICE] increasedCount : {}", increasedCount);

        // redis 입장 목록에 입장한 사용자 PK 추가
        stageDataUtil.addStageEnterUserSet(user.getId());
        return increasedCount;
    }

    /**
     * 스테이지 진행 상태 확인 로직
     * @return
     */
    public StageModel.StageInfo getStageInfo() {
        log.info("[SERVICE] getStageInfo");
        String status = stageDataUtil.getStageStatus();

        Long statusStartTime = stageDataUtil.getStageStatusStartTime(status);
//        Long statusElapsedTime = null;
//        if(statusStartTime != null) {
//            statusElapsedTime = System.nanoTime() - statusStartTime;
//        }
        Long statusElapsedTime = (statusStartTime!=null) ? System.nanoTime() - statusStartTime : null; // 위에 주석이랑 같은 기능 코드

        //음악 정보
        Music music = stageDataUtil.getStageMusic();

        return StageModel.StageInfo.toModel(status, statusElapsedTime, music);
    }


    /**
     * 스테이지 입장 로직 2 : 중복 입장 불가 버전
     * @param user
     * @return
     */
//    public int addStageUser(User user) {
//        log.info("[SERVICE] addAndGetStageUserCount");
//
//        int userCount = stageRoutineService.getStageUserCount();
//
//        if(stageDataService.isStageExistUser(user.getId())){
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
        String stageStatus = stageDataUtil.getStageStatus();
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


    /**
     * 스테이지 참여자 고유값 목록 확인 로직
     * @return
     */
    public List<Long> getStageEnterUserIds() {
        log.info("[SERVICE] getStageEnterUserProfiles");
        Set<String> userIdSet = stageDataUtil.getStageEnterUsers();
        List<String> userIds = new ArrayList<>(userIdSet);
        return userIds.stream().map(Long::parseLong).collect(Collectors.toList());
    }


    /**
     * 스테이지 캐치 등록 로직
     * @param user
     */
    public void registerCatch(User user) {
        log.info("[SERVICE] registerCatch");

        if(!stageDataUtil.getStageStatus().equals(StageRoutineService.STAGE_STATUS_CATCH)) {
            throw new StageException(StageStatusCode.STAGE_STATUS_NOT_CATCH);
        }

        final long now = System.currentTimeMillis();
        log.info("registerCatch user id : {}, nickname : {}, time : {}", user.getId(), user.getNickname(), now);
        redisDao.setValuesZSet(StageDataUtil.KEY_STAGE_CATCH_USER_LIST, user.getId().toString(), (int) now);
//        redisDao.setValuesHash(StageRoutineService.STAGE_CATCH_USER_LIST, (int) now, user);
    }

}
