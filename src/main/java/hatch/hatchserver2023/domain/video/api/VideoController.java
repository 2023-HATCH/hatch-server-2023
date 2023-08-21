package hatch.hatchserver2023.domain.video.api;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.video.VideoCacheUtil;
import hatch.hatchserver2023.domain.video.application.HashtagService;
import hatch.hatchserver2023.domain.like.application.LikeService;
import hatch.hatchserver2023.domain.video.application.VideoService;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.video.dto.VideoModel;
import hatch.hatchserver2023.domain.video.dto.VideoResponseDto;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
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

    private final VideoCacheUtil videoCacheUtil;

    public VideoController(VideoService videoService, HashtagService hashtagService, LikeService likeService, VideoCacheUtil videoCacheUtil){
        this.videoService = videoService;
        this.hashtagService = hashtagService;
        this.likeService = likeService;
        this.videoCacheUtil = videoCacheUtil;
    }

    /**
     * 조회수 증가 api
     * @param videoId
     * @return
     */
    @PreAuthorize("hasAnyRole('ROLE_ANONYMOUS', 'ROLE_USER')")
    @GetMapping("/{videoId}/view")
    public ResponseEntity<CommonResponse> addViewCount(@PathVariable UUID videoId) {
        Video video = videoService.findOne(videoId);
        videoCacheUtil.addViewCount(video);
        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.ADD_VIEW_COUNT_SUCCESS
        ));
    }


    /**
     * 영상 상세 조회
     * - uuid로 조회
     *
     * @param videoId
     * @return video
     */
    @PreAuthorize("hasAnyRole('ROLE_ANONYMOUS', 'ROLE_USER')")
    @GetMapping("/{videoId}")
    public ResponseEntity<CommonResponse> getOneVideo(@AuthenticationPrincipal User user,
                                                @PathVariable UUID videoId) {
        Video video = videoService.findOne(videoId);


        // 로그인한 유저이면, 영상에 좋아요를 눌렀는지 확인
        // 비회원이면, isLike는 언제나 false
        boolean isLiked = user != null && likeService.isAlreadyLiked(video, user);

        int likeCount = videoCacheUtil.getLikeCount(video);
        int commentCount = videoCacheUtil.getCommentCount(video);
        int viewCount = videoCacheUtil.getViewCount(video);

        VideoModel.VideoInfo videoInfo = VideoModel.VideoInfo.builder()
                                                            .likeCount(likeCount)
                                                            .isLiked(isLiked)
                                                            .commentCount(commentCount)
                                                            .video(video)
                                                            .viewCount(viewCount)
                                                            .build();

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.GET_VIDEO_DETAIL_SUCCESS,
                VideoResponseDto.GetVideo.toDto(videoInfo)
        ));
    }


    /**
     * 영상 삭제
     *
     * @param uuid
     * @return isSuccess
     */
    @DeleteMapping("/{uuid}")
    public ResponseEntity<CommonResponse> deleteVideo(@PathVariable UUID uuid){
        videoService.deleteOne(uuid);

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.VIDEO_DELETE_SUCCESS,
                VideoResponseDto.IsSuccess.toDto(true)
        ));
    }

    /**
     * 영상 목록 조회
     * - 홈에서 사용할 api
     * - 최신순 조회
     *
     * @param user, pageable
     * @return videoList
     */
    @PreAuthorize("hasAnyRole('ROLE_ANONYMOUS', 'ROLE_USER')")
    @GetMapping
    public ResponseEntity<CommonResponse> getVideoList(@AuthenticationPrincipal User user,
                                                       Pageable pageable){

        Slice<VideoModel.VideoInfo> slice = videoService.findByCreatedAt(user, pageable);

        StatusCode statusCode = user == null ? VideoStatusCode.GET_VIDEO_LIST_SUCCESS_FOR_ANONYMOUS : VideoStatusCode.GET_VIDEO_LIST_SUCCESS_FOR_USER;

        return ResponseEntity.ok(CommonResponse.toResponse(
                statusCode,
                VideoResponseDto.GetVideoList.toDto(VideoResponseDto.GetVideo.toDtos(slice.getContent()), slice.isLast())
        ));
    }

    /**
     * 영상 목록 조회
     * - 검색 화면에서 사용할 api
     *
     * @param user, pageable
     * @return
     */
    @PreAuthorize("hasAnyRole('ROLE_ANONYMOUS', 'ROLE_USER')")
    @GetMapping("/random")
    public ResponseEntity<CommonResponse> getRandomVideoList(@AuthenticationPrincipal User user,
                                                             Pageable pageable) {

        Slice<VideoModel.VideoInfo> slice = videoService.findByRandom(user, pageable);

        StatusCode statusCode = user == null ? VideoStatusCode.GET_VIDEO_LIST_SUCCESS_FOR_ANONYMOUS : VideoStatusCode.GET_VIDEO_LIST_SUCCESS_FOR_USER;

        return ResponseEntity.ok(CommonResponse.toResponse(
                statusCode,
                VideoResponseDto.GetVideoList.toDto(VideoResponseDto.GetVideo.toDtos(slice.getContent()), slice.isLast())
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
    public ResponseEntity<CommonResponse> uploadVideo(@AuthenticationPrincipal User user,
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
    public ResponseEntity<CommonResponse> uploadVideo1(@AuthenticationPrincipal User user,
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
    public ResponseEntity<CommonResponse> uploadVideo2(@AuthenticationPrincipal User user,
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



    /**
     * 해시태그로 영상 검색
     *
     * @param tag
     * @param pageable
     * @param pageable
     * @return videoList
     */
    @PreAuthorize("hasAnyRole('ROLE_ANONYMOUS', 'ROLE_USER')")
    @GetMapping("/search")
    public ResponseEntity<CommonResponse> searchByHashtag(@AuthenticationPrincipal User user,
                                                          @RequestParam String tag,
                                                          Pageable pageable) {

        Slice<VideoModel.VideoInfo> slice = hashtagService.searchHashtag(tag, user, pageable);

        StatusCode statusCode = user == null ? VideoStatusCode.HASHTAG_SEARCH_SUCCESS_FOR_ANONYMOUS : VideoStatusCode.HASHTAG_SEARCH_SUCCESS_FOR_USER;

        return ResponseEntity.ok(CommonResponse.toResponse(
                statusCode,
                VideoResponseDto.GetVideoList.toDto(VideoResponseDto.GetVideo.toDtos(slice.getContent()), slice.isLast())
        ));
    }


    /**
     * 모든 해시태그 목록 전달
     * -해시태그 검색하기 전에 사용
     *
     * @return tagList
     */
    @GetMapping("/tags")
    public ResponseEntity<CommonResponse> getHashtagList() {
        List<String> tagList = hashtagService.getHashtagList();

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.GET_HASHTAG_LIST_SUCCESS,
                VideoResponseDto.GetHashtagList.toDto(tagList)
        ));
    }

}
