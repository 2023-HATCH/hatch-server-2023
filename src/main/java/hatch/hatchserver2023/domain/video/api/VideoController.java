package hatch.hatchserver2023.domain.video.api;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.video.application.HashtagService;
import hatch.hatchserver2023.domain.video.application.LikeService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final LikeService likeService;

    public VideoController(VideoService videoService, HashtagService hashtagService, LikeService likeService){
        this.videoService = videoService;
        this.hashtagService = hashtagService;
        this.likeService = likeService;
    }


    /**
     * 영상 상세 조회
     * - 회원용
     * - uuid로 조회
     *
     * @param uuid
     * @return video
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping("/{uuid}")
    public ResponseEntity<?> getOneVideoForUser(@AuthenticationPrincipal User user,
                                                @PathVariable UUID uuid) {
        Video video = videoService.findOne(uuid);
        boolean isLiked = likeService.isAlreadyLiked(video, user);

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.GET_VIDEO_DETAIL_SUCCESS,
                VideoResponseDto.GetVideo.toDto(video, isLiked)
        ));
    }

    /**
     * 영상 상세 조회
     * - 비회원용
     * - uuid로 조회
     *
     * @param uuid
     * @return video
     */
    @PreAuthorize("hasAnyRole('ROLE_ANONYMOUS')")
    @GetMapping("/anonymous/{uuid}")
    public ResponseEntity<?> getOneVideoForAnonymous(@AuthenticationPrincipal User user,
                                                     @PathVariable UUID uuid) {
        Video video = videoService.findOne(uuid);
        boolean isLiked = false;

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.GET_VIDEO_DETAIL_SUCCESS,
                VideoResponseDto.GetVideo.toDto(video, isLiked)
        ));
    }


    /**
     * 영상 삭제
     *
     * @param uuid
     * @return isSuccess
     */
    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteVideo(@PathVariable UUID uuid){
        videoService.deleteOne(uuid);

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.VIDEO_DELETE_SUCCESS,
                VideoResponseDto.IsSuccess.toDto(true)
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


    /**
     * 영상 업로드 - 정식
     * - MultipartFile에 @RequestPart 적용
     * - 영상 업로드, 썸네일 추출&업로드, 해시태그 파싱
     *
     * @param user
     * @param video
     * @param title
     * @param tag
     * @return
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")    //로그인한 사용자만 사용 가능
    @PostMapping
    public ResponseEntity<?> uploadVideo(@AuthenticationPrincipal User user,
                                          @RequestPart MultipartFile video,
                                          @RequestParam String title,
                                          @RequestParam String tag) {
        log.info("[VideoController][upload video] Request multiPartFile's ContentType >> " + video.getContentType());

        Video createdVideo = videoService.createVideo(video, user, title, tag);

        // 해시태그 파싱 후 저장
        hashtagService.saveHashtag(tag, createdVideo);

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.VIDEO_UPLOAD_SUCCESS,
                VideoResponseDto.VideoUuid.toDto(createdVideo)));
    }

    /**
     * 영상 업로드 - 임시 방법 1
     * - parameter(url) 사용
     * - 영상 업로드, 썸네일 추출&업로드, 해시태그 파싱
     *
     * @param user
     * @param video
     * @param title
     * @param tag
     * @return video_uuid
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")    //로그인한 사용자만 사용 가능
    @PostMapping("/upload1")
    public ResponseEntity<?> uploadVideo1(@AuthenticationPrincipal User user,
                                          @RequestParam MultipartFile video,
                                          @RequestParam String title,
                                          @RequestParam String tag) {
        log.info("[VideoController][upload video] Request multiPartFile's ContentType >> " + video.getContentType());

        Video createdVideo = videoService.createVideo(video, user, title, tag);

        // 해시태그 파싱 후 저장
        hashtagService.saveHashtag(tag, createdVideo);

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.VIDEO_UPLOAD_SUCCESS,
                VideoResponseDto.VideoUuid.toDto(createdVideo)));
    }

    /**
     * 영상 업로드 - 임시 방법 2
     * - 아무런 매핑 없이 사용
     * - 영상 업로드, 썸네일 추출&업로드, 해시태그 파싱
     *
     * @param user
     * @param video
     * @param title
     * @param tag
     * @return video_uuid
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @PostMapping("/upload2")
    public ResponseEntity<?> uploadVideo2(@AuthenticationPrincipal User user,
                                          MultipartFile video,
                                          String title,
                                          String tag) {
        log.info("[VideoController][upload video] Request multiPartFiles ContentType >> " + video.getContentType());

        Video createdVideo = videoService.createVideo(video, user, title, tag);

        // 해시태그 파싱 후 저장
        hashtagService.saveHashtag(tag, createdVideo);

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.VIDEO_UPLOAD_SUCCESS,
                VideoResponseDto.VideoUuid.toDto(createdVideo)));
    }


    //TODO: Pageable은 어떻게 적용할까나.. 아님 프론트에서 적용? Slice 적용?

    /**
     * 해시태그로 영상 검색
     *
     * @param tag
     * @param pageable
     * @return videoList
     */
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


    //해시태그 삭제 - 테스트용
    @DeleteMapping("/tags/{title}")
    public boolean deleteHashtag(@PathVariable String title){
        hashtagService.delete(title);
        return true;
    }
}
