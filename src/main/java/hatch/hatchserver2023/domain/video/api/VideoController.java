package hatch.hatchserver2023.domain.video.api;

import hatch.hatchserver2023.domain.video.application.HashtagService;
import hatch.hatchserver2023.domain.video.application.VideoService;
import hatch.hatchserver2023.domain.video.domain.Hashtag;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.video.dto.VideoResponseDto;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.VideoStatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("api/v1/videos")
public class VideoController {

    private final VideoService videoService;
    private final HashtagService hashtagService;

    public VideoController(VideoService videoService, HashtagService hashtagService){
        this.videoService = videoService;
        this.hashtagService = hashtagService;
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

        // 해시태그 파싱 후 저장
        hashtagService.saveHashtag(tag, createdVideo);

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

        // 해시태그 파싱 후 저장
        hashtagService.saveHashtag(tag, createdVideo);
        // TODO: 작성자를 Video에 추가하는 것

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.VIDEO_UPLOAD_SUCCESS,
                VideoResponseDto.CreateVideo.toDto(createdVideo)));
    }


    //TODO: Pageable은 어떻게 적용할까나.. 아님 프론트에서 적용?
    @GetMapping("/search")
    public ResponseEntity<?> searchByHashtag(@RequestParam String tag, Pageable pageable) {
        List<Video> videoList = hashtagService.searchHashtag(tag);

        //videoList 출력
        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.GET_VIDEO_LIST_SUCCESS,
                VideoResponseDto.GetVideoList.toDto(videoList)
        ));
    }

    // 간이 해시태그 목록 조회 - 삭제 예정
    @GetMapping("/tags")
    public List<Hashtag> getHashtagList() {
        return hashtagService.getHashtagList();
    }
}
