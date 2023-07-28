package hatch.hatchserver2023.domain.stage.application;

import hatch.hatchserver2023.domain.stage.api.StageSocketResponser;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.repository.UserRepository;
import hatch.hatchserver2023.global.config.redis.RedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StageRoutineService {

    public static final String STAGE_ENTER_USER_COUNT = "STAGE_ENTER_USER_COUNT";
    public static final String STAGE_ENTER_USER_LIST = "STAGE_ENTER_USER_LIST";
    public static final String STAGE_CATCH_USER_LIST = "STAGE_CATCH_USER_LIST";

    public static final String STAGE_STATUS = "STAGE_STATUS";
    public static final String STAGE_STATUS_WAIT = "WAIT";
    public static final String STAGE_STATUS_CATCH = "CATCH";
    public static final String STAGE_STATUS_PLAY = "PLAY";
    public static final String STAGE_STATUS_MVP = "MVP";

    private static final int STAGE_CATCH_TIME = 3;
    private static final int STAGE_MVP_TIME = 7;
    private static final int STAGE_CATCH_SUCCESS_LAST_INDEX = 2;

    private final UserRepository userRepository;
    private final RedisDao redisDao;

    private final StageSocketResponser stageSocketResponser;

    public StageRoutineService(UserRepository userRepository, RedisDao redisDao, StageSocketResponser stageSocketResponser) {
        this.userRepository = userRepository;
        this.redisDao = redisDao;
        this.stageSocketResponser = stageSocketResponser;
    }

    /**
     * 스테이지 루틴 시작 및 반복 메서드
     */
    @Async //비동기 처리
    public void startRoutine() {
        log.info("StageRoutineUtil : stage routine start");
        while(getStageUserCount() >= 3) {
            try {
                // 캐치 시작
                startCatch();
                TimeUnit.SECONDS.sleep(STAGE_CATCH_TIME);
                endCatch();

                // 플레이 시작
                int musicTime = startPlay();
                TimeUnit.SECONDS.sleep(musicTime);
                endPlay();

                // MVP 시작
                startMVP();
                TimeUnit.SECONDS.sleep(STAGE_MVP_TIME);
                endMVP(); // stage status 변경, player 및 mvp 데이터 정리, 다음 캐치 여부 결정?
            } catch (InterruptedException interruptedException) {
                log.info("StageRoutineUtil ERROR interruptedException : {}", interruptedException.getMessage());
            }
        }
        log.info("StageRoutineUtil : userCount < 3, end Stage routine");
        redisDao.deleteValues(STAGE_STATUS);

        stageSocketResponser.stageRoutineStop();
    }


    private void startCatch() {
        log.info("StageRoutineUtil startCatch");
        setStageStatus(STAGE_STATUS_CATCH);
        stageSocketResponser.startCatch("개발중");
    }

    private void endCatch() {
        log.info("StageRoutineUtil endCatch");

        // TODO : 개발 편의를 위해 인원 검사 안함
//        if(Integer.parseInt(redisDao.getValues(STAGE_ENTER_USER_COUNT))<3 || redisDao.getSetSize(STAGE_CATCH_USER_LIST)<3) {
//            setStageStatus(STAGE_STATUS_WAIT);
//            log.info("endCatch : change stage status to WAIT. enter or catch user count is less then 3");
//            return;
//        }

        Set<String> userIds = redisDao.getValuesZSet(STAGE_CATCH_USER_LIST, 0, STAGE_CATCH_SUCCESS_LAST_INDEX);
        log.info("endCatch userIds : {}", userIds);
        List<User> users = userRepository.findAllById(userIds.stream().map(Long::parseLong).collect(Collectors.toList()));
        log.info("endCatch users nickname : {}", users.stream().map(User::getNickname).collect(Collectors.toList()));
        stageSocketResponser.endCatch(users);
        redisDao.deleteValues(STAGE_CATCH_USER_LIST);
    }

    private int startPlay() {
        log.info("StageRoutineUtil startPlay");
        redisDao.setValues(STAGE_STATUS, STAGE_STATUS_PLAY);
        stageSocketResponser.startPlay("개발중");
        int musicTime = 1;
        return musicTime;
    }

    private void endPlay() {
        log.info("StageRoutineUtil endPlay");
    }

    private void startMVP() {
        log.info("StageRoutineUtil startMVP");
        redisDao.setValues(STAGE_STATUS, STAGE_STATUS_MVP);
        stageSocketResponser.startMVP("개발중");
    }

    private void endMVP() {
        log.info("StageRoutineUtil endMVP");

        // 사용자 목록이 3명 미만이면 스테이지 대기상태로 변경
        Long size = redisDao.getSetSize(StageRoutineService.STAGE_ENTER_USER_LIST);
//        log.info("tempCheckStageEmpty STAGE_ENTER_USER_LIST set size : {}", size);
        if(size < 3) {
//            log.info("endMVP set STAGE_ENTER_USER_COUNT = 0");
            redisDao.setValues(STAGE_STATUS, STAGE_STATUS_WAIT);
        }
    }


    public int getStageUserCount() {
        String countString = redisDao.getValues(StageRoutineService.STAGE_ENTER_USER_COUNT);
        log.info("StageRoutineUtil countString : {}", countString);
        return (countString==null) ? 0 : Integer.parseInt(countString);
    }

    public void setStageStatus(String status) {
        redisDao.setValues(STAGE_STATUS, status);
    }
}
