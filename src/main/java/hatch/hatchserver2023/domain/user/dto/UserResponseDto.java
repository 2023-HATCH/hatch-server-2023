package hatch.hatchserver2023.domain.user.dto;

import hatch.hatchserver2023.domain.user.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserResponseDto {

    @ToString
    @Getter
    @Builder
    public static class KakaoLogin { //응답dto 에도 생성자?
        private UUID userId;
        private String nickname;
        private String email;
        private String profileImg;

        public static KakaoLogin toDto(User user) {
            return KakaoLogin.builder()
                    .userId(user.getUuid())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .profileImg(user.getProfileImg())
                    .build();
        }
    }

    @ToString
    @Getter
    @Builder
    public static class SimpleUserProfile {
        private UUID userId;
        private String nickname;
        private String profileImg;

        // ObjectMapper 로 json 변환을 위한 생성자 2개
        public SimpleUserProfile(UUID userId, String nickname, String profileImg) {
            this.userId = userId;
            this.nickname = nickname;
            this.profileImg = profileImg;
        }

        public SimpleUserProfile() {
        }

        public static List<SimpleUserProfile> toDtos(List<User> users) {
            return users.stream().map(SimpleUserProfile::toDto).collect(Collectors.toList());
        }

        public static SimpleUserProfile toDto(User user) {
            return SimpleUserProfile.builder()
                    .userId(user.getUuid())
                    .nickname(user.getNickname())
                    .profileImg(user.getProfileImg())
                    .build();
        }
    }

    @ToString
    @Getter
    @Builder
    // 영상 커뮤니티에서 사용자를 화면에 보이기 위해 필요한 정보
    public static class CommunityUserInfo {
        private UUID userId;
        private String nickname;
        private String email;
        private String profileImg;

        public static CommunityUserInfo toDto(User user){
            return CommunityUserInfo.builder()
                    .userId(user.getUuid())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .profileImg(user.getProfileImg())
                    .build();
        }

    }


    @ToString
    @Getter
    @Builder
    public static class CommunityUserInfoList {

        private List<CommunityUserInfo> userList;

        public static CommunityUserInfoList toDto(List<User> users) {

            List<CommunityUserInfo> userInfoList = users.stream()
                    .map(CommunityUserInfo::toDto)
                    .collect(Collectors.toList());

            return CommunityUserInfoList.builder()
                    .userList(userInfoList)
                    .build();
        }
    }


    @ToString
    @Getter
    @Builder
    public static class IsSuccess {
        private Boolean isSuccess;

        public static IsSuccess toDto(Boolean isSuccess) {
            return IsSuccess.builder()
                    .isSuccess(isSuccess)
                    .build();
        }
    }

    @ToString
    @Getter
    @Builder
    public static class FollowList {

        private List<FollowUserInfo> followerList;
        private List<FollowUserInfo> followingList;

        public static FollowList toDto(List<FollowUserInfo> follower, List<FollowUserInfo> following) {
            return FollowList.builder()
                    .followerList(follower)
                    .followingList(following)
                    .build();
        }
    }

    @ToString
    @Getter
    @Builder
    public static class FollowUserInfo {

        private UUID userId;
        private String nickname;
        private String email;
        private String introduce;
        private String profileImg;
        private Boolean isFollowing;

        public static FollowUserInfo toDto(User user, Boolean isFollowing){
            return FollowUserInfo.builder()
                    .userId(user.getUuid())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .introduce(user.getIntroduce())
                    .profileImg(user.getProfileImg())
                    .isFollowing(isFollowing)
                    .build();
        }
    }
}
