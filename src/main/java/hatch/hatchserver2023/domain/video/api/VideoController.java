package hatch.hatchserver2023.domain.video.api;

import hatch.hatchserver2023.domain.video.application.VideoService;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.video.dto.VideoResponseDto;
import hatch.hatchserver2023.domain.video.repository.VideoRepository;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.VideoStatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("api/v1/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService){
        this.videoService = videoService;
    }


    /**
     * 영상 상세 조회
     * - uuid로 조회함
     *
     * @param uuid
     * @return video response dto
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<?> getOneVideo(@PathVariable UUID uuid) {
        Video video = videoService.findOne(uuid);

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.GET_VIDEO_DETAIL_SUCCESS,
                VideoResponseDto.GetVideo.toDto(video)
        ));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteVideo(@PathVariable UUID uuid){
        Boolean isSuccess = videoService.deleteOne(uuid);

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.GET_VIDEO_DETAIL_SUCCESS,
                isSuccess
        ));
    }

    /**
     * 영상 목록 조회
     * - 홈에서 사용할 api
     * - 최신순 조회 (변경 가능)(좋아요 순이 나을지.. 고민고민)
     *
     * @param pageable
     * @return
     */
    @GetMapping
    public ResponseEntity<?> getVideoList(Pageable pageable){
        Slice<Video> slice = videoService.findByCreatedTime(pageable);

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.GET_VIDEO_LIST_SUCCESS,
                VideoResponseDto.GetVideoList.toDto(slice)
        ));
    }

    /**
     * 영상 목록 조회
     * - 검색 화면에서 사용할 api
     *
     * @param pageable
     * @return
     */
    @GetMapping("/random")
    public ResponseEntity<?> getRandomVideoList(Pageable pageable) {
        Slice<Video> slice = videoService.findByRandom(pageable);

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.GET_VIDEO_LIST_SUCCESS,
                VideoResponseDto.GetVideoList.toDto(slice)
        ));
    }




    @PostMapping("/upload1")
    public ResponseEntity<?> uploadVideo1(@RequestParam MultipartFile video,
                                         @RequestParam String title,
                                         @RequestParam String tag) {
        log.info("[VideoController][upload video] Request multiPartFile's ContentType >> " + video.getContentType());

        Video createdVideo = videoService.createVideo(video, title, tag);

        // TODO: 해시태그 파싱해서 따로 저장하는 것 추가
        // TODO: 작성자를 Video에 추가하는 것

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.VIDEO_UPLOAD_SUCCESS,
                VideoResponseDto.CreateVideo.toDto(createdVideo)));
    }

    @PostMapping("/upload2")
    public ResponseEntity<?> uploadVideo2(MultipartFile video,
                                         String title,
                                         String tag) {
        log.info("[VideoController][upload video] Request multiPartFiles ContentType >> " + video.getContentType());

        Video createdVideo = videoService.createVideo(video, title, tag);

        // TODO: 해시태그 파싱해서 따로 저장하는 것 추가
        // TODO: 작성자를 Video에 추가하는 것

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.VIDEO_UPLOAD_SUCCESS,
                VideoResponseDto.CreateVideo.toDto(createdVideo)));
    }
}
