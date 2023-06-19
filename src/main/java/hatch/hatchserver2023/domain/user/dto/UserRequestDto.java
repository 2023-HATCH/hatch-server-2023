package hatch.hatchserver2023.domain.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

public class UserRequestDto {

    @Data
    public static class KakaoLogin {
        @NotBlank
        private String kakaoAccessToken;
    }
}
