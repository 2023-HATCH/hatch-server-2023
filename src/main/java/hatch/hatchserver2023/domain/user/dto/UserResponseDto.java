package hatch.hatchserver2023.domain.user.dto;

import hatch.hatchserver2023.domain.user.domain.User;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

public class UserResponseDto {

    @ToString
    @Getter
    @Builder
    public static class KakaoLogin { //응답dto 에도 생성자?
        private UUID uuid;
        private String nickname;
        private String email;

        public static KakaoLogin toDto(User user) {
            return KakaoLogin.builder()
                    .uuid(user.getUuid())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
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

        public static SimpleUserProfile toDto(User user) {
            return SimpleUserProfile.builder()
                    .userId(user.getUuid())
                    .nickname(user.getNickname())
                    .profileImg(user.getProfileImg())
                    .build();
        }
    }
}
