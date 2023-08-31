package hatch.hatchserver2023.domain.user.api;


import hatch.hatchserver2023.domain.like.application.LikeService;
import hatch.hatchserver2023.domain.user.application.UserUtilService;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.UserModel;
import hatch.hatchserver2023.domain.user.dto.UserRequestDto;
import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
import hatch.hatchserver2023.domain.video.dto.VideoModel;
import hatch.hatchserver2023.domain.video.dto.VideoResponseDto;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/v1/users")
public class UserController {

    private final UserUtilService userUtilService;
    private final LikeService likeService;

    public UserController(UserUtilService userUtilService, LikeService likeService) {
        this.userUtilService = userUtilService;
        this.likeService = likeService;
    }

    /**
     * 전체 유저 조회
     *
     * @param loginUser
     * @return userList except login user
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping("/all")
    public ResponseEntity<CommonResponse> getAllUser(@AuthenticationPrincipal User loginUser) {
        List<User> userList = userUtilService.findAll(loginUser);

        return ResponseEntity.ok(CommonResponse.toResponse(
                UserStatusCode.GET_ALL_USER_SUCCESS,
                UserResponseDto.UserSearchInfoList.toDto(userList)
        ));
    }


    /**
     * 프로필 조회
     *
      * @param loginUser
     * @param userId
     * @return user_profile
     */
    @PreAuthorize("hasAnyRole('ROLE_ANONYMOUS', 'ROLE_USER')")
    @GetMapping("/profile/{userId}")
    public ResponseEntity<CommonResponse> getProfile(@AuthenticationPrincipal User loginUser,
                                                     @PathVariable @NotBlank UUID userId) {

        UserModel.ProfileInfo profileInfo = userUtilService.getProfile(userId, loginUser);

        return ResponseEntity.ok(CommonResponse.toResponse(
                UserStatusCode.GET_PROFILE_SUCCESS,
                UserResponseDto.Profile.toDto(profileInfo)
        ));
    }



    /**
     * 업로드한 영상 목록 조회
     * + 페이지네이션
     *
     * @param loginUser
     * @param userId
     * @param pageable
     * @return
     */
    @PreAuthorize("hasAnyRole('ROLE_ANONYMOUS', 'ROLE_USER')")
    @GetMapping("/videos/{userId}")
    public ResponseEntity<CommonResponse> getUsersVideoList(@AuthenticationPrincipal User loginUser,
                                                            @PathVariable @NotBlank UUID userId,
                                                            Pageable pageable) {

        Slice<VideoModel.VideoInfo> videoSlice = userUtilService.getUsersVideoList(userId, loginUser, pageable);

        StatusCode statusCode = loginUser == null ? UserStatusCode.GET_USERS_VIDEO_LIST_SUCCESS_FOR_ANONYMOUS : UserStatusCode.GET_USERS_VIDEO_LIST_SUCCESS_FOR_USER;

        return ResponseEntity.ok(CommonResponse.toResponse(
                statusCode,
                VideoResponseDto.GetVideoList.toDto(VideoResponseDto.GetVideo.toDtos(videoSlice.getContent()), videoSlice.isLast())
        ));
    }


    /**
     * 프로필 수정
     * - introduce, instagramId, twitterId 전체 수정
     *
     * @param user
     * @param request
     * @return success
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @PatchMapping("/me")
    public ResponseEntity<CommonResponse> updateMyProfile(@AuthenticationPrincipal User user,
                                                          @RequestBody UserRequestDto.UpdateProfile request) {

        log.info("[UserController][Update Profile] request >> " + request);

        userUtilService.updateProfile(user, request.getIntroduce(), request.getInstagramId(), request.getTwitterId());

        return ResponseEntity.ok(CommonResponse.toResponse(
                UserStatusCode.UPDATE_MY_PROFILE_SUCCESS,
                UserResponseDto.IsSuccess.toDto(true)
        ));
    }


    /**
     * 검색 - 계정 검색
     * -닉네임 1순위, 이메일 2순위로 검색
     *
     * @param key
     * @param pageable
     * @return
     */
    @GetMapping("/search")
    public ResponseEntity<CommonResponse> searchUsers(@RequestParam @NotBlank String key,
                                                      Pageable pageable) {

        List<User> userList = userUtilService.searchUsers(key, pageable);

        return ResponseEntity.ok(CommonResponse.toResponse(
                UserStatusCode.SEARCH_USERS_SUCCESS,
                UserResponseDto.UserSearchInfoList.toDto(userList)));
    }

    /**
     * 팔로우 등록
     *
     * @param fromUser
     * @param userId
     * @return isSuccess
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @PostMapping("/follow/{userId}")
    public ResponseEntity<CommonResponse> addFollow(@AuthenticationPrincipal User fromUser,
                                                    @PathVariable @NotBlank UUID userId) {

        User toUser = userUtilService.findOneByUuid(userId);

        userUtilService.addFollow(fromUser, toUser);

        return ResponseEntity.ok(CommonResponse.toResponse(
                UserStatusCode.ADD_FOLLOW_SUCCESS,
                UserResponseDto.IsSuccess.toDto(true)));
    }


    /**
     * 팔로우 삭제
     *
     * @param fromUser
     * @param userId
     * @return isSuccess
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @DeleteMapping("/follow/{userId}")
    public ResponseEntity<CommonResponse> deleteFollow(@AuthenticationPrincipal User fromUser,
                                                       @PathVariable @NotBlank UUID userId) {
        User toUser = userUtilService.findOneByUuid(userId);

        userUtilService.deleteFollow(fromUser, toUser);

        return ResponseEntity.ok(CommonResponse.toResponse(
                UserStatusCode.DELETE_FOLLOW_SUCCESS,
                UserResponseDto.IsSuccess.toDto(true)));
    }



    /**
     * 팔로워/팔로잉 목록 조회
     * - 로그인한 사용자가 다른 사용자를 팔로우를 했는지 여부를 isFollowing로 제공
     *
     * @param loginUser
     * @param userId
     * @return followerList, followingList
     */
    @PreAuthorize("hasAnyRole('ROLE_ANONYMOUS', 'ROLE_USER')")
    @GetMapping("/follow/{userId}")
    public ResponseEntity<CommonResponse> getFollowList(@AuthenticationPrincipal User loginUser,
                                                        @PathVariable @NotBlank UUID userId) {

        User user = userUtilService.findOneByUuid(userId);
        List<UserModel.FollowInfo> follower = userUtilService.getFollowerList(user, loginUser);
        List<UserModel.FollowInfo> following = userUtilService.getFollowingList(user, loginUser);

        StatusCode statusCode = loginUser == null ? UserStatusCode.GET_FOLLOW_LIST_SUCCESS_FOR_ANONYMOUS : UserStatusCode.GET_FOLLOW_LIST_SUCCESS_FOR_USER;

        return ResponseEntity.ok(CommonResponse.toResponse(
                statusCode,
                UserResponseDto.FollowList.toDto(UserResponseDto.FollowUserInfo.toDtos(follower), UserResponseDto.FollowUserInfo.toDtos(following))
        ));
    }
}
