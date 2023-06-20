package hatch.hatchserver2023.domain.user.application;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hatch.hatchserver2023.domain.user.dto.KakaoDto;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import hatch.hatchserver2023.global.common.response.exception.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

@Slf4j
//@RequiredArgsConstructor
@Service
public class KakaoService {
    private final String USER_INFO_REQUEST_URL = "https://kapi.kakao.com/v2/user/me";

    /** access_token을 이용하여 사용자 정보 조회
     *
     * @param token : 프론트로부터 받은 카카오 액세스 토큰
     * @return 카카오로부터 가져온 사용자 정보
     */
    public KakaoDto.GetUserInfo getUserInfo(String token) {
        log.info("[SERVICE] getUserInfo");

        KakaoDto.GetUserInfo kakaoUser;

        try {
            // 요청
            HttpURLConnection conn = makeConnection(token);

            // 요청의 응답을 json 형태로 읽어오기
            JsonElement element = getJsonElementFromResponse(conn.getInputStream());
            log.info("kakao json element : {}", element);

            // 응답으로부터 카카오 회원번호, nickname, profileIm, email(선택) 가져옴
            JsonObject kakao_account = element.getAsJsonObject().get("kakao_account").getAsJsonObject();
            JsonObject profile = kakao_account.getAsJsonObject().get("profile").getAsJsonObject();

            Long id = getKakaoAccountNumber(element);
            String nickname = getNickname(profile);
            String profileImg = getProfileImg(profile);
            String email = getEmail(kakao_account);

            log.info("id : {}", id);
            log.info("nickname : {}", nickname);
            log.info("profileImg : {}", profileImg);
            log.info("email : {}", email);

            // dto
            kakaoUser = KakaoDto.GetUserInfo.builder()
                    .kakaoAccountNumber(id)
                    .nickname(nickname)
                    .profileImg(profileImg)
                    .email(email)
                    .build();

//            br.close(); //TODO : 추후 삭제

        } catch (Exception e) {
            log.warn("getKakaoUserInfo failed : {}", e.getMessage());
            log.warn(Arrays.toString(e.getStackTrace()));
            throw new AuthException(UserStatusCode.KAKAO_CONNECTION_FAIL);
        }

        return kakaoUser;

    }

    private HttpURLConnection makeConnection(String token) throws IOException {
        URL url = new URL(USER_INFO_REQUEST_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization", "Bearer " + token); //전송할 header 작성, access_token전송

        //TODO : 이 부분 에러 캐치하기
        //결과 코드가 200이라면 성공
        int responseCode = conn.getResponseCode();
        if(responseCode != 200){
            log.warn("kakao connection failed : {} {}", conn.getResponseCode(), conn.getResponseMessage());
        }

        return conn;
    }

    private JsonElement getJsonElementFromResponse(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";

        while ((line = br.readLine()) != null) {
            result += line;
        }

        br.close(); //TODO : 여기서 닫아도 괜찮을 것 같음

        // String을 json으로 파싱
        return JsonParser.parseString(result);

//        JsonParser parser = new JsonParser();
//        return parser.parse(result);
    }

    private String getEmail(JsonObject kakao_account) {
        String email = "";
        boolean hasEmail = kakao_account.getAsJsonObject().get("has_email").getAsBoolean();
        if (hasEmail) {
            email = kakao_account.getAsJsonObject().get("email").getAsString();
        }
        return email;
    }

    private String getProfileImg(JsonObject profile) {
        JsonElement profileImg = profile.getAsJsonObject().get("profile_image_url");
        if (profileImg == null) {
            return "";
        }
        return profileImg.getAsString();
    }

    private String getNickname(JsonObject profile) {
//            JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
//            boolean hasNickname = properties.getAsJsonObject().get("has_nickname").getAsBoolean();
//            String nickname = "";
//            if (hasNickname) {
//                nickname = profile.getAsJsonObject().get("nickname").getAsString();
//            }
        JsonElement nickname = profile.getAsJsonObject().get("nickname") ;
        if (nickname == null) {
            throw new AuthException(UserStatusCode.KAKAO_NICKNAME_EMPTY);
        }
        return nickname.getAsString();
    }

    private Long getKakaoAccountNumber(JsonElement element) {
        return element.getAsJsonObject().get("id").getAsLong();
    }
}
