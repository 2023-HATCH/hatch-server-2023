package hatch.hatchserver2023.domain.video.api;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.video.application.HashtagService;
import hatch.hatchserver2023.domain.like.application.LikeService;
import hatch.hatchserver2023.domain.video.application.VideoService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


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
     * - uuid로 조회
     *
     * @param videoId
     * @return video
     */
    @PreAuthorize("hasAnyRole('ROLE_ANONYMOUS', 'ROLE_USER')")
    @GetMapping("/{videoId}")
    public ResponseEntity<CommonResponse> getOneVideoForUser(@AuthenticationPrincipal User user,
                                                @PathVariable UUID videoId) {
        Video video = videoService.findOne(videoId);
        boolean isLiked = false;

        // 로그인한 유저이면, 영상에 좋아요를 눌렀는지 확인
        // 비회원이면, isLike는 언제나 false
        if(user != null) {
            isLiked = likeService.isAlreadyLiked(video, user);

        }

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
     * @return
     */
    @PreAuthorize("hasAnyRole('ROLE_ANONYMOUS', 'ROLE_USER')")
    @GetMapping
    public ResponseEntity<CommonResponse> getVideoList(@AuthenticationPrincipal User user,
                                                       Pageable pageable){
        Slice<Video> slice = videoService.findByCreatedAt(pageable);

        //회원: 영상 좋아요 여부 liked 지정
        if (user != null) {

            List<VideoResponseDto.GetVideo> videoList = slice.stream()
                    .map(video -> VideoResponseDto.GetVideo.toDto(video, likeService.isAlreadyLiked(video, user)))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(CommonResponse.toResponse(
                    VideoStatusCode.GET_VIDEO_LIST_SUCCESS_FOR_USER,
                    VideoResponseDto.GetVideoList.toDto(videoList, slice.isLast())
            ));
        } else {
            //비회원: liked는 모두 false
            List<VideoResponseDto.GetVideo> videoList = slice.stream()
                    .map(video -> VideoResponseDto.GetVideo.toDto(video, false))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(CommonResponse.toResponse(
                    VideoStatusCode.GET_VIDEO_LIST_SUCCESS_FOR_ANONYMOUS,
                    VideoResponseDto.GetVideoList.toDto(videoList, slice.isLast())
            ));
        }
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

        Slice<Video> slice = videoService.findByRandom(pageable);

        //회원: 영상 좋아요 여부 liked 지정
        if (user != null) {

            List<VideoResponseDto.GetVideo> videoList = slice.stream()
                    .map(video -> VideoResponseDto.GetVideo.toDto(video, likeService.isAlreadyLiked(video, user)))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(CommonResponse.toResponse(
                    VideoStatusCode.GET_VIDEO_LIST_SUCCESS_FOR_USER,
                    VideoResponseDto.GetVideoList.toDto(videoList, slice.isLast())
            ));
        } else {
            //비회원: liked는 모두 false
            List<VideoResponseDto.GetVideo> videoList = slice.stream()
                    .map(video -> VideoResponseDto.GetVideo.toDto(video, false))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(CommonResponse.toResponse(
                    VideoStatusCode.GET_VIDEO_LIST_SUCCESS_FOR_ANONYMOUS,
                    VideoResponseDto.GetVideoList.toDto(videoList, slice.isLast())
            ));
        }
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

        Slice<Video> slice = hashtagService.searchHashtag(tag, pageable);

        //회원: 영상 좋아요 여부 liked 지정
        if (user != null) {

            List<VideoResponseDto.GetVideo> videoList = slice.stream()
                    .map(video -> VideoResponseDto.GetVideo.toDto(video, likeService.isAlreadyLiked(video, user)))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(CommonResponse.toResponse(
                    VideoStatusCode.HASHTAG_SEARCH_SUCCESS_FOR_USER,
                    VideoResponseDto.GetVideoList.toDto(videoList, slice.isLast())
            ));
        } else {
            //비회원: liked는 모두 false
            List<VideoResponseDto.GetVideo> videoList = slice.stream()
                    .map(video -> VideoResponseDto.GetVideo.toDto(video, false))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(CommonResponse.toResponse(
                    VideoStatusCode.HASHTAG_SEARCH_SUCCESS_FOR_ANONYMOUS,
                    VideoResponseDto.GetVideoList.toDto(videoList, slice.isLast())
            ));
        }
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
