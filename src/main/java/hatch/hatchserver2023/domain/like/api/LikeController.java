package hatch.hatchserver2023.domain.like.api;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.like.application.LikeService;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.video.dto.VideoResponseDto;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import hatch.hatchserver2023.global.common.response.code.VideoStatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("api/v1/likes")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService){
        this.likeService = likeService;
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

        likeService.addLike(videoId, user);

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


        likeService.deleteLike(videoId, user);

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.LIKE_DELETE_SUCCESS,
                VideoResponseDto.IsSuccess.toDto(true)
        ));
    }


    /**
     * 사용자가 좋아요 누른 영상 목록 조회
     *
     * @param user
     * @param userId
     * @param pageable
     * @return videoList
     */
    @PreAuthorize("hasAnyRole('ROLE_ANONYMOUS', 'ROLE_USER')")
    @GetMapping("/{userId}")
    public ResponseEntity<CommonResponse> getLikedVideoList(@AuthenticationPrincipal User user,
                                                            @PathVariable UUID userId,
                                                            Pageable pageable) {

        Slice<Video> slice = likeService.getLikedVideoList(userId, pageable);

        //회원: 영상 좋아요 여부 liked 지정
        if (user != null) {

            List<VideoResponseDto.GetVideo> videoList = slice.stream()
                    .map(video -> VideoResponseDto.GetVideo.toDto(video, likeService.isAlreadyLiked(video, user)))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(CommonResponse.toResponse(
                    VideoStatusCode.GET_LIKE_VIDEO_LIST_SUCCESS_FOR_USER,
                    VideoResponseDto.GetVideoList.toDto(videoList, slice.isLast())
            ));
        } else {
            //비회원: liked는 모두 false
            List<VideoResponseDto.GetVideo> videoList = slice.stream()
                    .map(video -> VideoResponseDto.GetVideo.toDto(video, false))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(CommonResponse.toResponse(
                    VideoStatusCode.GET_LIKE_VIDEO_LIST_SUCCESS_FOR_ANONYMOUS,
                    VideoResponseDto.GetVideoList.toDto(videoList, slice.isLast())
            ));
        }
    }

}
