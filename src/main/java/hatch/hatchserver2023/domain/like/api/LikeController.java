package hatch.hatchserver2023.domain.like.api;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.like.application.LikeService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                                                            Pageable pageable){

        Slice<Video> slice = likeService.getLikedVideoList(userId ,pageable);

        //회원이면 좋아요를 눌렀는지 확인
        if (user != null) {
            // 각 인덱스에서 좋아요를 눌렀는지 아닌지를 가지고 있는 리스트
            List<Boolean> likeList = new ArrayList<>();

            for (Video video : slice) {
                likeList.add(likeService.isAlreadyLiked(video, user));
            }

            //좋아요 여부 리스트와 함께 DTO로 만듦
            return ResponseEntity.ok(CommonResponse.toResponse(
                    VideoStatusCode.GET_LIKE_VIDEO_LIST_SUCCESS,
                    VideoResponseDto.GetVideoList.toDto(slice, likeList)
            ));
        }

        //비회원이면, liked가 모두 false
        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.GET_LIKE_VIDEO_LIST_SUCCESS,
                VideoResponseDto.GetVideoList.toDto(slice)
        ));
    }

}
