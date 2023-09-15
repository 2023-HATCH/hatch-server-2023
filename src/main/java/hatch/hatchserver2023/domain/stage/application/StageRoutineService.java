package hatch.hatchserver2023.domain.stage.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import hatch.hatchserver2023.domain.stage.dto.AIModel;
import hatch.hatchserver2023.domain.stage.dto.StageModel;
import hatch.hatchserver2023.domain.stage.api.StageSocketResponser;
import hatch.hatchserver2023.domain.stage.domain.Music;
import hatch.hatchserver2023.domain.stage.repository.MusicRepository;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
import hatch.hatchserver2023.domain.user.repository.UserRepository;
import hatch.hatchserver2023.global.common.ObjectMapperUtil;
import hatch.hatchserver2023.global.common.response.code.StageStatusCode;
import hatch.hatchserver2023.global.common.response.exception.StageException;
import hatch.hatchserver2023.global.common.response.socket.StageStatusType;
import hatch.hatchserver2023.global.config.redis.RedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Transactional(readOnly = true)
@Service
public class StageRoutineService {

    public static final int STAGE_PLAYER_COUNT_VALUE = 3;
    public static final int STAGE_MID_SCORE_NUM_WHEN_ALL = -1;
    public static final int STAGE_CATCH_SUCCESS_LAST_INDEX = 2;
    public static final int STAGE_MID_SCORE_TIME_INTERVAL = 4;

    private static final int STAGE_CATCH_TIME = 3;
    private static final int STAGE_CATCH_AGAIN_INTERVAL = 2;
    private static final int STAGE_MVP_TIME = 7;

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
//                int playTime = startPlay(music);
//                log.info("StageRoutineUtil sleep playTime : {}", playTime);
//                TimeUnit.SECONDS.sleep(playTime);
                
                int playTime = startPlay(music);
                repeatMidScore(playTime);

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
        stageDataUtil.setStageStatus(StageStatusType.CATCH);

        // 음악 랜덤 선정
        Music music = musicRepository.findRandomOne().get(0);
        stageDataUtil.setStageMusic(music);

        stageDataUtil.setStageStatusStartTime();
        stageSocketResponser.startCatch(music);
        return music;
    }

    private boolean endCatch() throws InterruptedException {
        log.info("StageRoutineUtil endCatch");
        stageDataUtil.setStageStatus(StageStatusType.CATCH_END);

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
        stageDataUtil.setStageStatus(StageStatusType.PLAY);

        final int readyTime = 5;
        int musicTime = (int) Math.ceil(music.getLength()/1000.0); //밀리초 올림해서 초단위로 변경 //TODO : 개발 편의 위해 잠시 //music.getLength()/100

        stageDataUtil.setStageStatusStartTime();
        stageSocketResponser.startPlay();

        return readyTime + musicTime;
    }

    private void repeatMidScore(int playTimeRemain) throws InterruptedException {
        int midScoreNum = 0;
        while(playTimeRemain > STAGE_MID_SCORE_TIME_INTERVAL) {
            TimeUnit.SECONDS.sleep(STAGE_MID_SCORE_TIME_INTERVAL);
            sendMidScore(midScoreNum);
            midScoreNum += 1;
            playTimeRemain -= STAGE_MID_SCORE_TIME_INTERVAL;
        }

        TimeUnit.SECONDS.sleep(playTimeRemain);
    }

    private void sendMidScore(int midScoreNum) {
        Map<Integer, Float> similarities = new HashMap<Integer, Float>();
        List<StageModel.SimilarityFrameCount> frameCounts = new ArrayList<>();
        int mvpPlayerNum = getSimilarityAndMvp(similarities, false, midScoreNum, frameCounts);

        List<StageModel.PlayerResultInfo> playerResultInfos = getPlayerResultInfos(similarities, frameCounts);
        stageSocketResponser.sendMidScore(mvpPlayerNum, playerResultInfos);
    }

    private void endPlay() {
        log.info("StageRoutineUtil endPlay");
        stageDataUtil.setStageStatus(StageStatusType.PLAY_END);

        stageSocketResponser.endPlay();
    }

    private void startMVP() {
        log.info("StageRoutineUtil startMVP");

        Map<Integer, Float> similarities = new HashMap<Integer, Float>();
        List<StageModel.SimilarityFrameCount> frameCounts = new ArrayList<>();
        int mvpPlayerNum = getSimilarityAndMvp(similarities, true, STAGE_MID_SCORE_NUM_WHEN_ALL, frameCounts);

        // redis 에서 playerNum 전부의 사용자 정보 가져와서 playerNum과 유사도 더한 플레이어 결과 정보로 만듦
        List<StageModel.PlayerResultInfo> playerResultInfos = getPlayerResultInfos(similarities, frameCounts);

        // 상태 변경, 응답
        stageDataUtil.setStageStatus(StageStatusType.MVP);

        stageDataUtil.setStageStatusStartTime();
        stageSocketResponser.startMVP(mvpPlayerNum, playerResultInfos);

        // 캐치, 플레이 데이터 초기화
        initDataAfterPlay();
    }

    private void endMVP() {
        log.info("StageRoutineUtil endMVP");
        stageDataUtil.setStageStatus(StageStatusType.MVP_END);
        redisDao.deleteValues(StageDataUtil.KEY_STAGE_MUSIC);
        stageSocketResponser.endMvp();

        // 입장자 3명 미만이면 스테이지 대기상태로 변경
        int userCount = stageDataUtil.getStageEnterUserCount();
//        log.info("tempCheckStageEmpty STAGE_ENTER_USER_LIST set size : {}", size);
        if(userCount < 3) {
//            log.info("endMVP set STAGE_ENTER_USER_COUNT = 0");
            stageDataUtil.setStageStatus(StageStatusType.WAIT);
        }

        initDataAfterMvp();
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
    private int getSimilarityAndMvp(Map<Integer, Float> similarities, boolean isAll, int midScoreNum, List<StageModel.SimilarityFrameCount> frameCounts) {
        float maxSimilarity = -100;  // 계산할 스켈레톤이 없는 경우 -100 으로 응답됨
        int maxPlayerNum = 0; // 아무도 플레이 스켈레톤을 전송하지 않으면 playerNum 0 번 유저가 mvp 가 되도록 설정

        // 유사도 계산하여 mvp 정하기
        for(int i = 0; i<STAGE_PLAYER_COUNT_VALUE; i++){

            String previousIndex = null;
            int startIndex = 0; // 전체일 경우 0부터 시작
            if(!isAll){ // 전체가 아닐 경우
                // 스켈레톤 가져와야 하는 시작 지점 가져옴
                previousIndex = redisDao.getValues(StageDataUtil.KEY_STAGE_PLAYER_SKELETON_MID_INDEX + i);
                if(previousIndex!=null){
                    startIndex = Integer.parseInt(previousIndex)+1; // 직전 인덱스의 다음 인덱스부터 가져옴
                }
            }
            log.info("previousIndex : {}", previousIndex);
            log.info("startIndex : {}", startIndex);

            // redis 에 저장해둔 스켈레톤 가져옴
            Set<String> skeletonStringSet = getSkeletonData(i, startIndex);
            if(skeletonStringSet==null || skeletonStringSet.isEmpty()) { // 이 유저의 스켈레톤이 비어있을 경우
                // 사용된 프레임 수 정보 저장
                frameCounts.add(StageModel.SimilarityFrameCount.toDto(0, 0));
                continue;
            } else {
                // 이번에 가져온 스켈레톤 개수 + 이전 인덱스 - 1로 인덱스 값 업데이트
                int nextIndex = startIndex+skeletonStringSet.size()-1;
                redisDao.setValues(StageDataUtil.KEY_STAGE_PLAYER_SKELETON_MID_INDEX + i, nextIndex);
                log.info("nextIndex : {}", nextIndex);
            }

            // 원래 자료형으로 형변환
            Float[][] skeletonFloatArray = skeletonToFloatArrays(skeletonStringSet);
            int userSkeletonSize = skeletonFloatArray.length;
            log.info("getSimilarityAndMvp : skeletonFloatArray size : {}", userSkeletonSize);
//            log.info("getSimilarityAndMvp : skeletonFloatArray : {}", skeletonFloatArray);
//            log.info("getSimilarityAndMvp skeletonFloatArray[0] : {}", skeletonFloatArray[0]);
//            log.info("getSimilarityAndMvp skeletonFloatArray[0][0] : {}", skeletonFloatArray[0][0]);
//            log.info("getSimilarityAndMvp skeletonFloatArray[0][0] : {}", skeletonFloatArray[1][0]);

            String title = stageDataUtil.getStageMusic().getTitle();
            // 유사도 계산
            float similarity;
//            float similarity=0f;
            AIModel.SimilarityCalculateInfo calculateInfo;
            try{
                calculateInfo = aiService.calculateSimilarity(title, skeletonFloatArray, midScoreNum);
                similarity = calculateInfo.getSimilarity();
                log.info("getSimilarityAndMvp : skeletonFloatArray size 2 : {}", skeletonFloatArray.length);
                log.info("getSimilarityAndMvp : music {} playerNum {} similarity : {}", title, i, similarity);
            }catch (NullPointerException e) {
                throw new StageException(StageStatusCode.MUSIC_NOT_FOUND);
            }

            // 유사도 모으기
            similarities.put(i, similarity);

            // mvp 선정
            if(maxSimilarity<similarity) {
                maxSimilarity = similarity;
                maxPlayerNum = i;
            }

            // 사용된 프레임 수 정보 저장
            frameCounts.add(StageModel.SimilarityFrameCount.toDto(userSkeletonSize, calculateInfo.getUsedAnswerFrameCount()));
        }

        log.info("getSimilarityAndMvp : frameCounts.size() : {}", frameCounts.size());
        return maxPlayerNum;
    }

    private Set<String> getSkeletonData(int i, int startIndex) {
        return redisDao.getValuesZSet(StageDataUtil.KEY_STAGE_PLAYER_SKELETON + i, startIndex, -1);
    }

    /**
     * 각 플레이어의 사용자정보, playerNum, 유사도를 합쳐서 유사도 응답으로 만드는 메서드
     * @param similarities
     * @return
     */
    private List<StageModel.PlayerResultInfo> getPlayerResultInfos(Map<Integer, Float> similarities, List<StageModel.SimilarityFrameCount> frameCounts) {
        List<StageModel.PlayerResultInfo> playerResultInfos = new ArrayList<>();
        for(int i=0; i<STAGE_PLAYER_COUNT_VALUE; i++) {
            try{
                UserResponseDto.SimpleUserProfile player = stageDataUtil.getPlayerUserInfo(i);
                Float similarity = similarities.get(i)==null ? -100f : similarities.get(i);
                StageModel.SimilarityFrameCount frameCount = frameCounts.get(i);

                StageModel.PlayerResultInfo playerResultInfo = StageModel.PlayerResultInfo.toDto(i, similarity, player, frameCount.getUsedUserFrameCount(), frameCount.getUsedAnswerFrameCount());
                playerResultInfos.add(playerResultInfo);
            } catch(StageException e){
                if(e.getCode() == StageStatusCode.FAIL_GET_PLAYER_USER_FROM_REDIS) {
                    log.info("getPlayerResultInfos : player of this playerNum not exist. skip");
                }
                else {
                    throw e;
                }
            }
        }
        return playerResultInfos;
    }



    /**
     * Redis 에서 가져온 스켈레톤 세트들 Set<String>을 AI서버에 맞게 Float[][] 형식으로 형변환하는 메서드
     * @param skeletonStringSet
     * @return
     */
    private Float[][] skeletonToFloatArrays(Set<String> skeletonStringSet) {
        List<Float[]> floatArrays = new ArrayList<>();

        //set 을 순서대로 돌면서 Float[] 로 만들고 floatArrays 에 모음
        for (String skeletonArrayString : skeletonStringSet) {
            // String -> List<Object> 로 형변환
            List skeletonPointObjects;
            try {
                skeletonPointObjects = objectMapperUtil.toOriginalType(skeletonArrayString, List.class); // Set 에 문자열로 저장되어있던 배열을 하나씩 꺼내 List로 변환 - Object형의 좌표값들로 구성된 List(스켈레톤 1개)
            } catch (JsonProcessingException e) {
                throw new StageException(StageStatusCode.FAIL_CONVERT_REDIS_SKELETON_DATA_TO_ARRAY);
            }

            // List<Object> -> List<Float> 로 형변환
            List<Float> floatList = (List<Float>) skeletonPointObjects.stream().map(value -> Float.parseFloat(value.toString())).collect(Collectors.toList());

            // 모으기 (List<List<Float>>)
            floatArrays.add(floatList.toArray(new Float[0]));
        }

        // List<List<Float>> -> Float[][] 로 형변환
        return floatArrays.toArray(new Float[0][0]);
    }

    /**
     * 캐치, 플레이 데이터 초기화 메서드
     */
    private void initDataAfterPlay() {
        for (int i=0; i<=STAGE_CATCH_SUCCESS_LAST_INDEX; i++) {
            redisDao.deleteValues(StageDataUtil.KEY_STAGE_PLAYER_SKELETON + i);
        }
    }

    /**
     * MVP 종료 후 이번 스테이지 데이터를 초기화하는 메서드
     */
    private void initDataAfterMvp() {
        redisDao.deleteValues(StageDataUtil.KEY_STAGE_PLAYER_INFO_HASH);
        for (int i=0; i<=STAGE_CATCH_SUCCESS_LAST_INDEX; i++) {
            redisDao.deleteValues(StageDataUtil.KEY_STAGE_PLAYER_SKELETON_MID_INDEX + i);
        }
    }

    private int getSendStageUserCount() {
        int userCount = stageDataUtil.getStageEnterUserCount();
        stageSocketResponser.userCount(userCount);
        return userCount;
    }

}
