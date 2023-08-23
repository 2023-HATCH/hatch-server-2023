package hatch.hatchserver2023.domain.user.api;

import hatch.hatchserver2023.domain.user.application.AuthService;
import hatch.hatchserver2023.domain.user.application.KakaoService;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.KakaoDto;
import hatch.hatchserver2023.domain.user.dto.UserRequestDto;
import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import hatch.hatchserver2023.global.common.response.exception.AuthException;
import hatch.hatchserver2023.global.config.redis.RedisFCMTokenDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Slf4j
@Validated
//@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final String LOGIN_TYPE_KAKAO = "kakao";

    private final KakaoService kakaoService;
    private final AuthService authService;
    private final RedisFCMTokenDao redisFCMTokenDao;

    public AuthController(KakaoService kakaoService, AuthService authService, RedisFCMTokenDao redisFCMTokenDao) {
        this.kakaoService = kakaoService;
        this.authService = authService;
        this.redisFCMTokenDao = redisFCMTokenDao;
    }

    /**
     * 카카오 회원가입 & 로그인 한번에 진행되는 api
     * @param type : "kakao" 고정값
     * @param requestDto
     * @param servletResponse
     * @return
     */
    @PostMapping("/login")
//    @PreAuthorize("hasAnyRole('ROLE_ANONYMOUS')")
    public ResponseEntity<CommonResponse> kakaoSignUpAndLogin(@RequestParam @NotBlank String type,
                                                              @RequestBody @Valid UserRequestDto.KakaoLogin requestDto,
                                                              HttpServletResponse servletResponse) {
        log.info("[API] POST /auth/login");

        validLoginType(type);

        KakaoDto.GetUserInfo userInfo = kakaoService.getUserInfo(requestDto.getKakaoAccessToken());
        User loginUser = authService.signUpAndLogin(userInfo, servletResponse);

        redisFCMTokenDao.saveToken(loginUser, requestDto.getFcmNotificationToken());

        return ResponseEntity.ok().body(CommonResponse.toResponse(UserStatusCode.KAKAO_LOGIN_SUCCESS, UserResponseDto.KakaoLogin.toDto(loginUser)));
    }

    private void validLoginType(String type) {
        if(!type.equals(LOGIN_TYPE_KAKAO)){
            throw new AuthException(UserStatusCode.LOGIN_TYPE_NOT_KAKAO);
        }
    }

}
