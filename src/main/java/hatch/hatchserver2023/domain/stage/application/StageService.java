package hatch.hatchserver2023.domain.stage.application;

import hatch.hatchserver2023.domain.stage.StageRoutineUtil;
import hatch.hatchserver2023.domain.stage.domain.Music;
import hatch.hatchserver2023.domain.stage.dto.AISimilarityRequestDto;
import hatch.hatchserver2023.domain.stage.dto.StageResponseDto;
import hatch.hatchserver2023.domain.stage.repository.MusicRepository;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.config.redis.RedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class StageService {

    @Autowired
    MusicRepository musicRepository;

    private final RedisDao redisDao;
    private final StageRoutineUtil stageRoutineUtil; //?

    // 환경변수 주입
    @Value("${AI_SERVER_URL}")
    private String AI_SERVER_URL;

    public StageService(RedisDao redisDao, StageRoutineUtil stageRoutineUtil) {
        this.redisDao = redisDao;
        this.stageRoutineUtil = stageRoutineUtil;
    }

    /**
     * 스테이지에서 댄스 정확도 계산
     *
     * @input music_title, sequence
     * @return similarity
     */
    // TODO: 어떤 사용자인지도 필요한가?
    public Float calculateSimilarity(String musicTitle, Float[][] sequence) {
        // 곡명으로 음악 찾기
        Music music = musicRepository.findByTitle(musicTitle);

        // ai 서버로 요청할 안무 두 개
        AISimilarityRequestDto requestDto = AISimilarityRequestDto.builder()
                .seq1(music.getAnswer())
                .seq2(sequence)
                .build();

        // ai 서버로 계산 요청
        WebClient client = WebClient.create(AI_SERVER_URL);

        ResponseEntity<StageResponseDto.GetSimilarity> response = client.post()
                .uri("/api/similarity")
                .bodyValue(requestDto)
                .retrieve()
                .toEntity(StageResponseDto.GetSimilarity.class)
                .block();

         return response.getBody().getSimilarity();
    }

    /**
     * 스테이지 입장 로직
     * @param user
     * @return
     */
    public int addStageUser(User user) {
        log.info("[SERVICE] addAndGetStageUserCount");

        // 인원수 increase
        String count = redisDao.getValues(StageRoutineUtil.STAGE_ENTER_USER_COUNT);
        log.info("[SERVICE] count : {}", count);

        int increasedCount = (count==null) ? 1 : Integer.parseInt(count)+1;
        redisDao.setValues(StageRoutineUtil.STAGE_ENTER_USER_COUNT, String.valueOf(increasedCount));
        log.info("[SERVICE] increasedCount : {}", increasedCount);
        
        // redis 입장 목록에 사용자 정보 추가
        redisDao.setValuesSet(StageRoutineUtil.STAGE_ENTER_USER_LIST, user.getUuid().toString());
        
        if(getStageStatus().equals(StageRoutineUtil.STAGE_STATUS_WAIT) && increasedCount >= 3) {
            log.info("stage user count >= 3");
            // stage 상태 관리 클래스의 catch start 기능 호출
            stageRoutineUtil.startRoutine();
        }
        return increasedCount;
    }

    /**
     * 스테이지 상태 확인 로직
     * @return
     */
    public String getStageStatus() {
        log.info("[SERVICE] getStageStatus");
        String stageStatus = redisDao.getValues(StageRoutineUtil.STAGE_STATUS);
        return (stageStatus==null) ? StageRoutineUtil.STAGE_STATUS_WAIT : stageStatus;
    }

    /**
     * 스테이지 참여자 목록 확인 로직
     * @return
     */
    public List<String> getStageEnterUsers() {
        log.info("[SERVICE] getStageEnterUserProfiles");
        Set<String> users = redisDao.getValuesSet(StageRoutineUtil.STAGE_ENTER_USER_LIST);
        return new ArrayList<>(users);
    }




}
