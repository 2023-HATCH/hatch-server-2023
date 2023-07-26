package hatch.hatchserver2023.domain.stage;

import hatch.hatchserver2023.global.config.redis.RedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service //TODO ?
public class StageRoutineUtil {

    private final RedisDao redisDao;

    private final SimpMessagingTemplate simpMessagingTemplate;

    public static final String STAGE_SEND_WS_URL = "/topic/stage";

    public static final String STAGE_ENTER_USER_COUNT = "STAGE_ENTER_USER_COUNT";
    public static final String STAGE_ENTER_USER_LIST = "STAGE_ENTER_USER_LIST";

    public static final String STAGE_STATUS = "STAGE_STATUS";
    public static final String STAGE_STATUS_WAIT = "STAGE_STATUS_WAIT";
    public static final String STAGE_STATUS_CATCH = "STAGE_STATUS_CATCH";
    public static final String STAGE_STATUS_PLAY = "STAGE_STATUS_PLAY";
    public static final String STAGE_STATUS_MVP = "STAGE_STATUS_MVP";

    public static final int STAGE_CATCH_TIME = 5;
    public static final int STAGE_MVP_TIME = 5;

    public StageRoutineUtil(RedisDao redisDao, SimpMessagingTemplate simpMessagingTemplate) {
        this.redisDao = redisDao;
        this.simpMessagingTemplate = simpMessagingTemplate;
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
                finishCatch();

                // 플레이 시작
                int musicTime = startPlay();
                TimeUnit.SECONDS.sleep(musicTime);
                finishPlay();

                // MVP 시작
                startMVP();
                TimeUnit.SECONDS.sleep(STAGE_MVP_TIME);
                finishMVP(); // stage status 변경, player 및 mvp 데이터 정리, 다음 캐치 여부 결정?
            } catch (InterruptedException interruptedException) {
                log.info("StageRoutineUtil ERROR interruptedException : {}", interruptedException.getMessage());
            }
        }
        log.info("StageRoutineUtil : userCount < 3, end Stage routine");
        redisDao.deleteValues(STAGE_STATUS);
        simpMessagingTemplate.convertAndSend(STAGE_SEND_WS_URL, "stage routine stop"); //TODO : DTO
    }


    private void startCatch() {
        log.info("StageRoutineUtil startCatch");
        redisDao.setValues(STAGE_STATUS, STAGE_STATUS_CATCH);
        simpMessagingTemplate.convertAndSend(STAGE_SEND_WS_URL, "catch start"); //TODO : DTO
    }

    private void finishCatch() {
        log.info("StageRoutineUtil finishCatch");
    }

    private int startPlay() {
        log.info("StageRoutineUtil startPlay");
        redisDao.setValues(STAGE_STATUS, STAGE_STATUS_PLAY);
        simpMessagingTemplate.convertAndSend(STAGE_SEND_WS_URL, "play start");
        int musicTime = 1;
        return musicTime;
    }

    private void finishPlay() {
        log.info("StageRoutineUtil finishPlay");
    }

    private void startMVP() {
        log.info("StageRoutineUtil startMVP");
        redisDao.setValues(STAGE_STATUS, STAGE_STATUS_MVP);
        simpMessagingTemplate.convertAndSend(STAGE_SEND_WS_URL, "mvp start");
    }

    private void finishMVP() {
        log.info("StageRoutineUtil finishMVP");
    }


    public int getStageUserCount() {
        String countString = redisDao.getValues(StageRoutineUtil.STAGE_ENTER_USER_COUNT);
        log.info("StageRoutineUtil countString : {}", countString);
        return (countString==null) ? 0 : Integer.parseInt(countString);
    }
}
