package hatch.hatchserver2023.domain.user.dto;

import hatch.hatchserver2023.domain.user.domain.User;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

public class UserResponseDto {

    @Builder
    @Data
    public static class KakaoLogin {
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
}
