package hatch.hatchserver2023.domain.stage.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hatch.hatchserver2023.domain.stage.api.StageSocketResponser;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
import hatch.hatchserver2023.domain.user.repository.UserRepository;
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
    public static final String KEY_STAGE_STATUS = "STAGE_STATUS";
    public static final String STAGE_STATUS_WAIT = "WAIT";
    public static final String STAGE_STATUS_CATCH = "CATCH";
    private static final String STAGE_STATUS_CATCH_END = "CATCH_END";
    public static final String STAGE_STATUS_PLAY = "PLAY";
    public static final String STAGE_STATUS_PLAY_END = "PLAY_END";
    public static final String STAGE_STATUS_MVP = "MVP";
    private static final String STAGE_STATUS_MVP_END = "MVP_END";

    public static final String KEY_STAGE_ENTER_USER_COUNT = "STAGE_ENTER_USER_COUNT";
    public static final String KEY_STAGE_ENTER_USER_LIST = "STAGE_ENTER_USER_LIST";
    public static final String KEY_STAGE_CATCH_USER_LIST = "STAGE_CATCH_USER_LIST";

    public static final String KEY_STAGE_PLAYER_INFO_HASH = "STAGE_PLAYER_INFO_HASH";
    public static final String KEY_STAGE_PLAYER_SKELETONS_PREFIX = "STAGE_PLAYER_SKELETONS_PREFIX";
    public static final int STAGE_PLAYER_COUNT_VALUE = 3;

    private static final int STAGE_CATCH_TIME = 3;
    private static final int STAGE_CATCH_AGAIN_INTERVAL = 2;
    private static final int STAGE_MVP_TIME = 7;
    private static final int STAGE_CATCH_SUCCESS_LAST_INDEX = 2;

    private final UserRepository userRepository;
    private final RedisDao redisDao;

    private final StageDataService stageDataService;
    private final AIService aiService;
    private final StageSocketResponser stageSocketResponser;

    public StageRoutineService(UserRepository userRepository, RedisDao redisDao, StageDataService stageDataService, AIService aiService, StageSocketResponser stageSocketResponser) {
        this.userRepository = userRepository;
        this.redisDao = redisDao;
        this.stageDataService = stageDataService;
        this.aiService = aiService;
        this.stageSocketResponser = stageSocketResponser;
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
                startCatch();
                TimeUnit.SECONDS.sleep(STAGE_CATCH_TIME);
                // 캐치한 사람이 없을 경우 2초 후 다시 캐치 시작
                if(!endCatch()) {
                    TimeUnit.SECONDS.sleep(STAGE_CATCH_AGAIN_INTERVAL);
                    continue;
                }

                // 플레이 시작
                int playTime = startPlay();
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
        redisDao.deleteValues(KEY_STAGE_STATUS);

        stageSocketResponser.stageRoutineStop();
    }


    private void startCatch() {
        log.info("StageRoutineUtil startCatch");
        stageDataService.setStageStatus(STAGE_STATUS_CATCH);
        stageSocketResponser.startCatch("개발중");
    }

    private boolean endCatch() throws InterruptedException {
        log.info("StageRoutineUtil endCatch");
        stageDataService.setStageStatus(STAGE_STATUS_CATCH_END);

        // TODO : 개발 편의를 위해 인원 검사 안함
//        checkUserCountInEndCatch();

        // 선착순 캐치 성공자 얻기
        Set<String> userIds = redisDao.getValuesZSet(KEY_STAGE_CATCH_USER_LIST, 0, STAGE_CATCH_SUCCESS_LAST_INDEX);

        // 아무도 캐치를 누르지 않은 경우
        if(userIds == null || userIds.size()==0){
            stageSocketResponser.endCatch();
            return false;
        }

        log.info("endCatch userIds : {}", userIds);
        List<User> users = userRepository.findAllById(userIds.stream().map(Long::parseLong).collect(Collectors.toList())); // 참고 : 이 List 의 인덱스 순서로 playerNum이 정해짐

        // user의 필요한 정보만 추출하여 Redis Hash에 플레이어 정보로 저장
        savePlayerInfo(users);

        // 응답, 데이터 정리
        stageSocketResponser.endCatch(users);
        redisDao.deleteValues(KEY_STAGE_CATCH_USER_LIST);
        return true;
    }

    private int startPlay() {
        log.info("StageRoutineUtil startPlay");
        redisDao.setValues(KEY_STAGE_STATUS, STAGE_STATUS_PLAY);
        stageSocketResponser.startPlay("개발중");
        final int readyTime = 5;
        int musicTime = 10; //TODO
        return readyTime + musicTime;
    }

    private void endPlay() {
        log.info("StageRoutineUtil endPlay");
        stageDataService.setStageStatus(STAGE_STATUS_PLAY_END);

        stageSocketResponser.endPlay();
    }

    private void startMVP() {
        log.info("StageRoutineUtil startMVP");

        int mvpPlayerNum = getMvpPlayerNum();

        // mvp 선정된 playerNum에 해당하는 플레이어 사용자정보 가져오기
        UserResponseDto.SimpleUserProfile mvpUser = getMvpUserInfo(mvpPlayerNum);

        // 상태 변경, 응답
        stageDataService.setStageStatus(STAGE_STATUS_MVP);
        stageSocketResponser.startMVP(mvpUser);

        // 캐치, 플레이 데이터 초기화
        initPlayData();
    }

    private void endMVP() {
        log.info("StageRoutineUtil endMVP");
        stageDataService.setStageStatus(STAGE_STATUS_MVP_END);
        stageSocketResponser.endMvp();

        // mvp 데이터 초기화 - 할 게 없음
        // initMvpData();

        // 사용자 목록이 3명 미만이면 스테이지 대기상태로 변경
        Long size = redisDao.getSetSize(StageRoutineService.KEY_STAGE_ENTER_USER_LIST);
//        log.info("tempCheckStageEmpty STAGE_ENTER_USER_LIST set size : {}", size);
        if(size < 3) {
//            log.info("endMVP set STAGE_ENTER_USER_COUNT = 0");
            redisDao.setValues(KEY_STAGE_STATUS, STAGE_STATUS_WAIT);
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
     * 플레이어 사용자 정보를 Redis Hash 에 저장하는 메서드
     * @param users
     */
    private void savePlayerInfo(List<User> users) {
        List<UserResponseDto.SimpleUserProfile> userSimples = users.stream().map(UserResponseDto.SimpleUserProfile::toDto).collect(Collectors.toList());
        for(int i=0; i<userSimples.size(); i++){ // i는 playerNum과 같음
            String userSimpleJson;
            try {
                userSimpleJson = new ObjectMapper().writeValueAsString(userSimples.get(i));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e); //TODO
            }
            redisDao.setValuesHash(KEY_STAGE_PLAYER_INFO_HASH, String.valueOf(i), userSimpleJson);
        }
    }

    /**
     * mvpPlayerNum에 해당하는 플레이어 사용자 정보 가져오기
     * @param mvpPlayerNum
     * @return
     */
    private UserResponseDto.SimpleUserProfile getMvpUserInfo(int mvpPlayerNum) {
        String userJson = redisDao.getValuesHash(KEY_STAGE_PLAYER_INFO_HASH, String.valueOf(mvpPlayerNum)).toString(); //TODO : nullPointException
        UserResponseDto.SimpleUserProfile mvpUser;
        try {
            mvpUser = new ObjectMapper().readValue(userJson, UserResponseDto.SimpleUserProfile.class); // TODO : ObjectMapper 사용 util 만들어서 모으기
        } catch (JsonProcessingException e) {
            throw new StageException(StageStatusCode.FAIL_GET_MVP_USER_INFO_FROM_REDIS_JSON);
        }
        return mvpUser;
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
            Set<String> skeletonStringSet = redisDao.getValuesZSetAll(KEY_STAGE_PLAYER_SKELETONS_PREFIX +i);
            if(skeletonStringSet==null) { // 이 유저의 스켈레톤이 비어있을 경우
                continue;
            }

            // 원래 자료형으로 형변환
            Float[][] skeletonFloatArray = skeletonToFloatArrays(skeletonStringSet);
//            log.info("endPlay skeletonFloatArray : {}", skeletonFloatArray);
//            log.info("endPlay skeletonFloatArray[0] : {}", skeletonFloatArray[0]);
//            log.info("endPlay skeletonFloatArray[0][0] : {}", skeletonFloatArray[0][0]);
//            log.info("endPlay skeletonFloatArray[0][0] : {}", skeletonFloatArray[1][0]);

            // 유사도 계산 TODO : 테스트 못해봄
//            float similarity;
            float similarity=0f;
            try{
//                similarity = aiService.calculateSimilarity("tempMusicTitle", skeletonFloatArray); //TODO
//                log.info("endPlay similarity {} : {}", i, similarity);
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
                list = new ObjectMapper().readValue(arrayString, List.class);
            } catch (JsonProcessingException e) {
                throw new StageException(StageStatusCode.FAIL_SAVE_MVP_USER_INFO_JSON);
            }

            // List<Object> -> List<Float> 로 형변환
            List<Float> floatList = (List<Float>) list.stream().map(value -> Float.parseFloat(value.toString())).collect(Collectors.toList());

            // 모으기
            floatArrays.add(floatList.toArray(new Float[0]));
        }

        return floatArrays.toArray(new Float[0][]);
    }

    /**
     * 캐치, 플레이 데이터 초기화 메서드
     */
    private void initPlayData() {
        for (int i=0; i<=STAGE_CATCH_SUCCESS_LAST_INDEX; i++) {
            redisDao.deleteValues(KEY_STAGE_PLAYER_SKELETONS_PREFIX +i);
        }
        redisDao.deleteValues(KEY_STAGE_PLAYER_INFO_HASH);
    }

    private int getSendStageUserCount() {
        int userCount = stageDataService.getStageUserCount();
        stageSocketResponser.userCount(userCount);
        return userCount;
    }

}
