package hatch.hatchserver2023.domain.stage.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import hatch.hatchserver2023.domain.stage.api.StageSocketResponser;
import hatch.hatchserver2023.domain.stage.domain.Music;
import hatch.hatchserver2023.domain.stage.repository.MusicRepository;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
import hatch.hatchserver2023.domain.user.repository.UserRepository;
import hatch.hatchserver2023.global.common.ObjectMapperUtil;
import hatch.hatchserver2023.global.common.response.code.StageStatusCode;
import hatch.hatchserver2023.global.common.response.exception.StageException;
import hatch.hatchserver2023.global.config.redis.RedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StageRoutineService {
    public static final String STAGE_STATUS_WAIT = "WAIT";
    public static final String STAGE_STATUS_CATCH = "CATCH";
    private static final String STAGE_STATUS_CATCH_END = "CATCH_END";
    public static final String STAGE_STATUS_PLAY = "PLAY";
    public static final String STAGE_STATUS_PLAY_END = "PLAY_END";
    public static final String STAGE_STATUS_MVP = "MVP";
    private static final String STAGE_STATUS_MVP_END = "MVP_END";

    public static final int STAGE_PLAYER_COUNT_VALUE = 3;

    private static final int STAGE_CATCH_TIME = 3;
    private static final int STAGE_CATCH_AGAIN_INTERVAL = 2;
    private static final int STAGE_MVP_TIME = 7;
    private static final int STAGE_CATCH_SUCCESS_LAST_INDEX = 2;

    private final UserRepository userRepository;
    private final MusicRepository musicRepository;
    private final RedisDao redisDao;

    private final StageDataUtil stageDataUtil;
    private final AIService aiService;

    private final StageSocketResponser stageSocketResponser;
    private final ObjectMapperUtil objectMapperUtil;

    public StageRoutineService(UserRepository userRepository, MusicRepository musicRepository, RedisDao redisDao, StageDataUtil stageDataUtil, AIService aiService, StageSocketResponser stageSocketResponser, ObjectMapperUtil objectMapperUtil) {
        this.userRepository = userRepository;
        this.musicRepository = musicRepository;
        this.redisDao = redisDao;
        this.stageDataUtil = stageDataUtil;
        this.aiService = aiService;
        this.stageSocketResponser = stageSocketResponser;
        this.objectMapperUtil = objectMapperUtil;
    }

    /**
     * 스테이지 루틴 시작 및 반복 메서드
     */
    @Async //비동기 처리
    public void startRoutine() {
        log.info("StageRoutineUtil : stage routine start");
        while(getSendStageUserCount() >= 3) {
            try {
                // 캐치 시작
                Music music = startCatch();
                TimeUnit.SECONDS.sleep(STAGE_CATCH_TIME);
                // 캐치한 사람이 없을 경우 2초 후 다시 캐치 시작
                if(!endCatch()) {
                    redisDao.deleteValues(StageDataUtil.KEY_STAGE_MUSIC); // 직전 캐치 음악 데이터 삭제
                    TimeUnit.SECONDS.sleep(STAGE_CATCH_AGAIN_INTERVAL);
                    continue;
                }

                // 플레이 시작
                int playTime = startPlay(music);
                TimeUnit.SECONDS.sleep(playTime);
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
        redisDao.deleteValues(StageDataUtil.KEY_STAGE_STATUS);

        stageSocketResponser.stageRoutineStop();
    }


    private Music startCatch() {
        log.info("StageRoutineUtil startCatch");
        stageDataUtil.setStageStatus(STAGE_STATUS_CATCH);

        // 음악 랜덤 선정
        Music music = musicRepository.findRandomOne().get(0);
        stageDataUtil.setStageMusic(music);

        stageDataUtil.setStageStatusStartTime();
        stageSocketResponser.startCatch(music);
        return music;
    }

    private boolean endCatch() throws InterruptedException {
        log.info("StageRoutineUtil endCatch");
        stageDataUtil.setStageStatus(STAGE_STATUS_CATCH_END);

        // TODO : 개발 편의를 위해 인원 검사 안함
//        checkUserCountInEndCatch();

        // 선착순 캐치 성공자 얻기
        Set<String> userIds = redisDao.getValuesZSet(StageDataUtil.KEY_STAGE_CATCH_USER_LIST, 0, STAGE_CATCH_SUCCESS_LAST_INDEX);

        // 아무도 캐치를 누르지 않은 경우
        if(userIds == null || userIds.size()==0){
            stageSocketResponser.endCatch();
            return false;
        }

        log.info("endCatch userIds : {}", userIds);
        List<User> users = userRepository.findAllById(userIds.stream().map(Long::parseLong).collect(Collectors.toList())); // 참고 : 이 List 의 인덱스 순서로 playerNum이 정해짐

        // user의 필요한 정보만 추출하여 Redis Hash에 플레이어 정보로 저장
        stageDataUtil.savePlayerInfo(users);

        // 응답, 데이터 정리
        stageSocketResponser.endCatch(users);
        redisDao.deleteValues(StageDataUtil.KEY_STAGE_CATCH_USER_LIST);
        return true;
    }

    private int startPlay(Music music) {
        log.info("StageRoutineUtil startPlay");
        stageDataUtil.setStageStatus(STAGE_STATUS_PLAY);

        final int readyTime = 5;
        int musicTime = 10; //TODO : 개발 편의 위해 잠시 //music.getLength()

        stageDataUtil.setStageStatusStartTime();
        stageSocketResponser.startPlay();

        return readyTime + musicTime;
    }

    private void endPlay() {
        log.info("StageRoutineUtil endPlay");
        stageDataUtil.setStageStatus(STAGE_STATUS_PLAY_END);

        stageSocketResponser.endPlay();
    }

    private void startMVP() {
        log.info("StageRoutineUtil startMVP");

        int mvpPlayerNum = getMvpPlayerNum();

        // mvp 선정된 playerNum에 해당하는 플레이어 사용자정보 가져오기
        UserResponseDto.SimpleUserProfile mvpUser = stageDataUtil.getMvpUserInfo(mvpPlayerNum);

        // 상태 변경, 응답
        stageDataUtil.setStageStatus(STAGE_STATUS_MVP);

        stageDataUtil.setStageStatusStartTime();
        stageSocketResponser.startMVP(mvpUser);

        // 캐치, 플레이 데이터 초기화
        initPlayData();
    }

    private void endMVP() {
        log.info("StageRoutineUtil endMVP");
        stageDataUtil.setStageStatus(STAGE_STATUS_MVP_END);
        redisDao.deleteValues(StageDataUtil.KEY_STAGE_MUSIC);
        stageSocketResponser.endMvp();

        // 입장자 3명 미만이면 스테이지 대기상태로 변경
        int userCount = stageDataUtil.getStageEnterUserCount();
//        log.info("tempCheckStageEmpty STAGE_ENTER_USER_LIST set size : {}", size);
        if(userCount < 3) {
//            log.info("endMVP set STAGE_ENTER_USER_COUNT = 0");
            stageDataUtil.setStageStatus(STAGE_STATUS_WAIT);
        }
    }


    ///// ======= util methods ======= /////

    /**
     * endCatch 시작점에서 사용자 수를 검사하는 메서드
     */
    private void checkUserCountInEndCatch() {
//        if(Integer.parseInt(redisDao.getValues(STAGE_ENTER_USER_COUNT))<3 || redisDao.getSetSize(STAGE_CATCH_USER_LIST)<3) {
//            setStageStatus(STAGE_STATUS_WAIT);
//            log.info("endCatch : change stage status to WAIT. enter or catch user count is less then 3");
//            return;
//        }
    }

    /**
     * 각 플레이어들의 유사도를 계산하여 MVP를 선정하고 MVP유저의 playerNum을 반환하는 메서드
     * @return
     */
    private int getMvpPlayerNum() {
        float maxSimilarity = -2;
        int maxPlayerNum = -1;

        // 유사도 계산하여 mvp 정하기
        for(int i = 0; i<STAGE_PLAYER_COUNT_VALUE; i++){
            // redis 에 저장해둔 스켈레톤 가져옴
            Set<String> skeletonStringSet = redisDao.getValuesZSetAll(StageDataUtil.KEY_STAGE_PLAYER_SKELETONS_PREFIX +i);
            if(skeletonStringSet==null) { // 이 유저의 스켈레톤이 비어있을 경우
                continue;
            }

            // 원래 자료형으로 형변환
            Float[][] skeletonFloatArray = skeletonToFloatArrays(skeletonStringSet);
//            log.info("endPlay skeletonFloatArray : {}", skeletonFloatArray);
//            log.info("endPlay skeletonFloatArray[0] : {}", skeletonFloatArray[0]);
//            log.info("endPlay skeletonFloatArray[0][0] : {}", skeletonFloatArray[0][0]);
//            log.info("endPlay skeletonFloatArray[0][0] : {}", skeletonFloatArray[1][0]);

            String title = stageDataUtil.getStageMusic().getTitle();
            // 유사도 계산 TODO : 테스트 못해봄
//            float similarity;
            float similarity=0f;
            try{
//                similarity = aiService.calculateSimilarity(title, skeletonFloatArray); //TODO
//                log.info("endPlay music {} user {} similarity : {}", title, i, similarity);
            }catch (NullPointerException e) {
                throw new StageException(StageStatusCode.MUSIC_NOT_FOUND);
            }

            // mvp 선정
            if(maxSimilarity<similarity) {
                maxSimilarity = similarity;
                maxPlayerNum = i;
            }
        }
        return maxPlayerNum;
    }


    /**
     * Redis 에서 가져온 스켈레톤 세트들 Set<String>을 AI서버에 맞게 Float[][] 형식으로 형변환하는 메서드
     * @param skeletonStringSet
     * @return
     */
    private Float[][] skeletonToFloatArrays(Set<String> skeletonStringSet) {
        List<Float[]> floatArrays = new ArrayList<>();

        //set 을 순서대로 돌면서 Float[] 로 만들고 floatArrays 에 모음
        for (String arrayString : skeletonStringSet) {
            // String -> List<Object> 로 형변환
            List list;
            try {
                list = objectMapperUtil.toOriginalType(arrayString, List.class);
            } catch (JsonProcessingException e) {
                throw new StageException(StageStatusCode.FAIL_SAVE_MVP_USER_INFO_JSON);
            }

            // List<Object> -> List<Float> 로 형변환
            List<Float> floatList = (List<Float>) list.stream().map(value -> Float.parseFloat(value.toString())).collect(Collectors.toList());

            // 모으기 (List<List<Float>>)
            floatArrays.add(floatList.toArray(new Float[0]));
        }

        // List<List<Float>> -> Float[][] 로 형변환
        return floatArrays.toArray(new Float[0][]);
    }

    /**
     * 플레이 데이터 초기화 메서드
     */
    private void initPlayData() {
        for (int i=0; i<=STAGE_CATCH_SUCCESS_LAST_INDEX; i++) {
            redisDao.deleteValues(StageDataUtil.KEY_STAGE_PLAYER_SKELETONS_PREFIX +i);
        }
        redisDao.deleteValues(StageDataUtil.KEY_STAGE_PLAYER_INFO_HASH);
    }

    private int getSendStageUserCount() {
        int userCount = stageDataUtil.getStageEnterUserCount();
        stageSocketResponser.userCount(userCount);
        return userCount;
    }

}
