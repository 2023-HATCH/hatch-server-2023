package hatch.hatchserver2023.domain.user.dto;

import hatch.hatchserver2023.domain.user.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

public class UserModel {

    @ToString
    @Builder
    @Getter
    public static class FollowInfo {

        private User user;
        private boolean isFollowing;

        public static FollowInfo toModel(User user, boolean isFollowing) {
            return FollowInfo.builder()
                    .user(user)
                    .isFollowing(isFollowing)
                    .build();
        }
    }

    @ToString
    @Builder
    @Getter
    public static class ProfileInfo {

        private User user;
        private boolean isMe;
        private boolean isFollowing;
        private int followingCount;
        private int followerCount;
    }
}
