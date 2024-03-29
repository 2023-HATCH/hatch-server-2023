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

//        @NotBlank
        private String fcmNotificationToken;

        public KakaoLogin() {
        }

        public KakaoLogin(String kakaoAccessToken, String fcmNotificationToken) {
            this.kakaoAccessToken = kakaoAccessToken;
            this.fcmNotificationToken = fcmNotificationToken;
        }
    }


    @ToString
    @Getter
    @Builder
    public static class UpdateProfile {

//        private String nickname;
//        private String email;
        private String introduce;
        private String instagramId;
        private String twitterId;

        public UpdateProfile () {}
        public UpdateProfile(String introduce, String instagramId, String twitterId) {
            this.introduce = introduce;
            this.instagramId = instagramId;
            this.twitterId = twitterId;
        }
    }

}
