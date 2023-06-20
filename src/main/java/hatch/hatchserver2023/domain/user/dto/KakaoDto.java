package hatch.hatchserver2023.domain.user.dto;

import hatch.hatchserver2023.domain.user.domain.User;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

public class KakaoDto {

    @ToString
    @Getter
    @Builder
    public static class GetUserInfo {
        @NotBlank
        private Long kakaoAccountNumber;

        @NotBlank
        private String nickname;

        private String profileImg;
        private String email;

        public GetUserInfo(Long kakaoAccountNumber, String nickname, String profileImg, String email) {
            this.kakaoAccountNumber = kakaoAccountNumber;
            this.nickname = nickname;
            this.profileImg = profileImg;
            this.email = email;
        }

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
