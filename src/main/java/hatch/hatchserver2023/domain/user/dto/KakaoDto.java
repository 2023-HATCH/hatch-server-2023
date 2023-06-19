package hatch.hatchserver2023.domain.user.dto;

import hatch.hatchserver2023.domain.user.domain.User;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

public class KakaoDto {
    @Data
    @Builder
    public static class GetUserInfo {
        @NotBlank
        private Long kakaoAccountNumber;

        @NotBlank
        private String nickname;

        private String profileImg;
        private String email;

        public User toUser() {
            return User.builder()
                    .kakaoAccountNumber(this.kakaoAccountNumber)
                    .nickname(this.nickname)
                    .profileImg(this.profileImg)
                    .email(this.email)
                    .build();
        }
    }
}
