package hatch.hatchserver2023.domain.like.api;

import hatch.hatchserver2023.domain.user.application.UserUtilService;
import hatch.hatchserver2023.domain.user.domain.User;
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

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/v1/likes")
public class LikeController {

    private final LikeService likeService;
    private final VideoService videoService;
    private final UserUtilService userUtilService;

    public LikeController(LikeService likeService, VideoService videoService, UserUtilService userUtilService){
        this.likeService = likeService;
        this.videoService = videoService;
        this.userUtilService = userUtilService;
    }


    /**
     * 좋아요 등록
     *
     * @param user
     * @param videoId
     * @return isSuccess
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")    //로그인한 사용자만 사용 가능
    @PostMapping("/{videoId}")
    public ResponseEntity<CommonResponse> addLike(@AuthenticationPrincipal User user,
                                     @PathVariable UUID videoId){

        Video video = videoService.findOne(videoId);
        likeService.addLike(video, user);

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.LIKE_ADD_SUCCESS,
                VideoResponseDto.IsSuccess.toDto(true)
        ));
    }


    /**
     * 좋아요 삭제
     *
     * @param user
     * @param videoId
     * @return isSuccess
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @DeleteMapping("/{videoId}")
    public ResponseEntity<CommonResponse> deleteLike(@AuthenticationPrincipal User user,
                                        @PathVariable UUID videoId){

        Video video = videoService.findOne(videoId);
        likeService.deleteLike(video, user);

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.LIKE_DELETE_SUCCESS,
                VideoResponseDto.IsSuccess.toDto(true)
        ));
    }


    /**
     * 사용자가 좋아요 누른 영상 목록 조회
     *
     * @param loginUser
     * @param userId
     * @param pageable
     * @return videoList
     */
    @PreAuthorize("hasAnyRole('ROLE_ANONYMOUS', 'ROLE_USER')")
    @GetMapping("/{userId}")
    public ResponseEntity<CommonResponse> getLikedVideoList(@AuthenticationPrincipal User loginUser,
                                                            @PathVariable UUID userId,
                                                            Pageable pageable) {

        User user = userUtilService.findOneByUuid(userId);
        Slice<VideoModel.VideoInfo> slice = likeService.getLikedVideoList(user, loginUser, pageable);

        StatusCode statusCode = loginUser == null ? VideoStatusCode.GET_LIKE_VIDEO_LIST_SUCCESS_FOR_ANONYMOUS : VideoStatusCode.GET_LIKE_VIDEO_LIST_SUCCESS_FOR_USER;

        return ResponseEntity.ok(CommonResponse.toResponse(
                statusCode,
                VideoResponseDto.GetVideoList.toDto(VideoResponseDto.GetVideo.toDtos(slice.getContent()), slice.isLast())
        ));
    }

}
