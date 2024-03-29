package hatch.hatchserver2023.domain.user.dto;

import hatch.hatchserver2023.domain.user.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;
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


    //계정 검색(CommunityInfo + introduce)
    //TODO: 그냥 리스트로 보내면 어떻게 보이는지 보고 생각해보자
    @ToString
    @Getter
    @Builder
    public static class UserSearchInfo {

        private UUID userId;
        private String nickname;
        private String email;
        private String profileImg;
        private String introduce;

        public static UserSearchInfo toDto(User user) {
            return UserSearchInfo.builder()
                    .userId(user.getUuid())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .profileImg(user.getProfileImg())
                    .introduce(user.getIntroduce())
                    .build();
        }
    }

    @ToString
    @Getter
    @Builder
    public static class UserSearchInfoList {

        private List<UserSearchInfo> userList;

        public static UserSearchInfoList toDto(List<User> users){

            List<UserSearchInfo> infoList = users.stream()
                                                .map(UserSearchInfo::toDto)
                                                .collect(Collectors.toList());

            return UserSearchInfoList.builder()
                    .userList(infoList)
                    .build();
        }
    }

    //프로필 조회
    @ToString
    @Getter
    @Builder
    public static class Profile {
        private UUID userId;
        private Boolean isMe;   //자기자신 프로필 조회 여부
        private Boolean isFollowing;   //현재 사용자가 해당 사용자를 팔로우하는지 여부
        private String nickname;
        private String email;
        private String profileImg;
        private String introduce;
        private String instagramId;
        private String twitterId;
        private int followingCount;
        private int followerCount;
        private ZonedDateTime createdAt;
        private ZonedDateTime modifiedAt;

        public static Profile toDto(UserModel.ProfileInfo profileInfo) {
            User user = profileInfo.getUser();

            return Profile.builder()
                    .userId(user.getUuid())
                    .isMe(profileInfo.isMe())
                    .isFollowing(profileInfo.isFollowing())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .profileImg(user.getProfileImg())
                    .introduce(user.getIntroduce())
                    .instagramId(user.getInstagramAccount())
                    .twitterId(user.getTwitterAccount())
                    .followerCount(profileInfo.getFollowerCount())
                    .followingCount(profileInfo.getFollowingCount())
                    .createdAt(user.getCreatedAt())
                    .modifiedAt(user.getModifiedAt())
                    .build();
        }
    }


    @ToString
    @Getter
    @Builder
    public static class IsSuccess {
        private Boolean success;

        public static IsSuccess toDto(Boolean isSuccess) {
            return IsSuccess.builder()
                    .success(isSuccess)
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

        //FollowInfo Model -> ResponseDto
        public static List<FollowUserInfo> toDtos(List<UserModel.FollowInfo> followInfoList) {
            return followInfoList.stream()
                            .map(one -> FollowUserInfo.toDto(one.getUser(), one.isFollowing()))
                            .collect(Collectors.toList());
        }
    }
}
