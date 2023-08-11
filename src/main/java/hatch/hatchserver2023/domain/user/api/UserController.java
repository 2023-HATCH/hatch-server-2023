package hatch.hatchserver2023.domain.user.api;


import hatch.hatchserver2023.domain.like.application.LikeService;
import hatch.hatchserver2023.domain.user.application.UserUtilService;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.UserRequestDto;
import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.video.dto.VideoResponseDto;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

        User user = userUtilService.findOneByUuid(userId);

        // 프로필 조회하는 주체가 자기자신의 프로필을 보는지 여부
        Boolean isMe = false;
        if(loginUser != null){
            isMe = loginUser.getUuid().equals(user.getUuid());
        }

        //팔로워 수, 팔로잉 수
        //TODO: 팔로워, 팔로잉 count를 조회할 때 마다 하는 방식으로 하는데, redis를 쓰던지, db에 저장하던지 다른 방식을 생각해봐야함
        int followerCount = userUtilService.countFollower(user);
        int followingCount = userUtilService.countFollowing(user);

        return ResponseEntity.ok(CommonResponse.toResponse(
                UserStatusCode.GET_PROFILE_SUCCESS,
                UserResponseDto.Profile.toDto(user, isMe, followingCount, followerCount)
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

        Slice<Video> videoSlice = userUtilService.getUsersVideoList(userId, pageable);

        //회원: 영상 좋아요 여부 liked 지정
        if(loginUser != null){

            List<VideoResponseDto.GetVideo> videoList = videoSlice.stream()
                    .map(video -> VideoResponseDto.GetVideo.toDto(video, likeService.isAlreadyLiked(video, loginUser)))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(CommonResponse.toResponse(
                    UserStatusCode.GET_USERS_VIDEO_LIST_SUCCESS_FOR_USER,
                    VideoResponseDto.GetVideoList.toDto(videoList, videoSlice.isLast())
            ));
        } else {
            //비회원: liked는 모두 false
            List<VideoResponseDto.GetVideo> videoList = videoSlice.stream()
                    .map(video -> VideoResponseDto.GetVideo.toDto(video, false))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(CommonResponse.toResponse(
                    UserStatusCode.GET_USERS_VIDEO_LIST_SUCCESS_FOR_ANONYMOUS,
                    VideoResponseDto.GetVideoList.toDto(videoList, videoSlice.isLast())
            ));
        }
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
                UserResponseDto.CommunityUserInfoList.toDto(userList)));
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
    //TODO: dto로 만드는 과정 리팩토링 필요한듯. service.isFollowing()을 responseDto에 넣을 수 없어서 이렇게 됨
    public ResponseEntity<CommonResponse> getFollowList(@AuthenticationPrincipal User loginUser,
                                                        @PathVariable @NotBlank UUID userId) {

        User user = userUtilService.findOneByUuid(userId);
        List<User> follower = userUtilService.getFollowerList(user);
        List<User> following = userUtilService.getFollowingList(user);

        //비회원: isFollowing 모두 false
        if (loginUser == null) {
            List<UserResponseDto.FollowUserInfo> followerList = follower.stream()
                    .map(one -> UserResponseDto.FollowUserInfo.toDto(one, false))
                    .collect(Collectors.toList());

            List<UserResponseDto.FollowUserInfo> followingList = following.stream()
                    .map(one -> UserResponseDto.FollowUserInfo.toDto(one, false))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(CommonResponse.toResponse(
                    UserStatusCode.GET_FOLLOW_LIST_SUCCESS_FOR_ANONYMOUS,
                    UserResponseDto.FollowList.toDto(followerList, followingList)
            ));
        }

        //회원: isFollowing 여부 함께 제공
        List<UserResponseDto.FollowUserInfo> followerList = follower.stream()
                .map(one -> UserResponseDto.FollowUserInfo.toDto(one, userUtilService.isFollowing(loginUser, one)))
                .collect(Collectors.toList());

        List<UserResponseDto.FollowUserInfo> followingList = following.stream()
                .map(one -> UserResponseDto.FollowUserInfo.toDto(one, userUtilService.isFollowing(loginUser, one)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(CommonResponse.toResponse(
                UserStatusCode.GET_FOLLOW_LIST_SUCCESS_FOR_USER,
                UserResponseDto.FollowList.toDto(followerList, followingList)
        ));
    }
}
