package hatch.hatchserver2023.domain.stage.api;

import hatch.hatchserver2023.domain.stage.domain.Music;
import hatch.hatchserver2023.domain.stage.dto.MusicRequestDto;
import hatch.hatchserver2023.domain.stage.dto.MusicResponseDto;
import hatch.hatchserver2023.domain.stage.repository.MusicRepository;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.CommonCode;
import hatch.hatchserver2023.global.common.response.exception.DefaultException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/v1/musics")
@RequiredArgsConstructor
public class MusicController {

    private final MusicRepository musicRepository;

    /**
     * 음악 저장을 위한 API
     * 앱에서 쓰이진 않고 스테이지 음악을 DB에 저장할 때 쓰임
     * 음악 파일은 s3에 직접 올린 후 s3 url만 DB에 저장함
     *
     *  @input title, singer, length, answer, concept, music_url
      * @return success
     */
    @PostMapping("/save")
    public ResponseEntity<Object> saveMusic(@RequestBody MusicRequestDto request) {

        Music music = request.toEntity();
        musicRepository.save(music);

        return ResponseEntity.ok(CommonResponse.toResponse(CommonCode.OK, music.getTitle() + " 음악 저장 성공"));

    }

    /**
     * 음악 목록 조회 API
     * 테스트용으로 제작
     * 실제 서비스에서 사용되지는 않을 예정
     *
     * @return musicList
     */
    @GetMapping("/list")
    public List<Music> getList() {
        return musicRepository.findAll();
    }


    /**
     * 음악 재생 테스트
     *
     * input: 음악 uuid, 재생 시각(milliseconds 단위)
     * output: 음악 제목, 음악 링크, 음악 길이(milliseconds 단위), 재생 시각(milliseconds 단위)
     *
     * @param uuid
     * @param playTime
     * @return title, musicUrl, length, playTime
     */
    @GetMapping("/play/{uuid}")
    public ResponseEntity<CommonResponse> playMusicTest(@PathVariable UUID uuid,
                                                        @RequestParam(defaultValue="3000") Integer playTime) {
        Music music = musicRepository.findByUuid(uuid);

        return ResponseEntity.ok(CommonResponse.toResponse(CommonCode.OK, MusicResponseDto.Play.toDto(music, playTime)));
    }
}
