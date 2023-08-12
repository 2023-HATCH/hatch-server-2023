package hatch.hatchserver2023.domain.stage.application;

import hatch.hatchserver2023.domain.stage.domain.Music;
import hatch.hatchserver2023.domain.stage.dto.AISimilarityRequestDto;
import hatch.hatchserver2023.domain.stage.dto.StageRequestDto;
import hatch.hatchserver2023.domain.stage.dto.StageResponseDto;
import hatch.hatchserver2023.domain.stage.repository.MusicRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
public class AIService {
    // 환경변수 주입
    @Value("${AI_SERVER_URL}")
    private String AI_SERVER_URL;

    private final MusicRepository musicRepository;

    public AIService(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
    }

    /**
     * 스테이지에서 댄스 정확도 계산 by Float[][]
     *
     * @return similarity
     * @input music_title, sequence
     */
    // TODO: 어떤 사용자인지도 필요한가?
//    public Float calculateSimilarity(String musicTitle, Float[][] sequence) {
    public Float calculateSimilarity(String musicTitle, Float[][] sequence) {
        // 곡명으로 음악 찾기
        Music music = musicRepository.findByTitle(musicTitle);

        // ai 서버로 요청할 안무 두 개
        AISimilarityRequestDto requestDto = AISimilarityRequestDto.builder()
                .seq1(music.getAnswer())
                .seq2(sequence)
                .build();

//        log.info("calculateSimilarity : requestDto");
//        log.info(requestDto.toString());

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
     * 스테이지에서 댄스 정확도 계산 by List<StageRequestDto.Skeleton>
     *
     * @return similarity
     * @input music_title, sequence
     */
    // TODO: 어떤 사용자인지도 필요한가?
//    public Float calculateSimilarity(String musicTitle, Float[][] sequence) {
    public Float calculateSimilarity(String musicTitle, List<StageRequestDto.Skeleton> skeletons) {
        // 곡명으로 음악 찾기
        Music music = musicRepository.findByTitle(musicTitle);

        // ai 서버로 요청할 안무 두 개
        AISimilarityRequestDto requestDto = AISimilarityRequestDto.builder()
                .seq1(music.getAnswer())
                .seq2(StageRequestDto.Skeleton.toAIFloatArrays(skeletons))
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
}