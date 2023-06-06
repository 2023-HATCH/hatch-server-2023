package hatch.hatchserver2023.domain.stage.api;

import hatch.hatchserver2023.domain.stage.domain.Music;
import hatch.hatchserver2023.domain.stage.dto.MusicRequestDto;
import hatch.hatchserver2023.domain.stage.repository.MusicRepository;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.CommonCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/v1/musics")
public class MusicController {

    @Autowired
    MusicRepository musicRepository;

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
}
