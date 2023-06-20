package hatch.hatchserver2023.domain.user.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

public class UserRequestDto {

    @ToString
    @Getter
    @Builder
    public static class KakaoLogin {
        @NotBlank
        private String kakaoAccessToken;

        public KakaoLogin() {
        }
        public KakaoLogin(String kakaoAccessToken) {
            this.kakaoAccessToken = kakaoAccessToken;
        }
    }

}
