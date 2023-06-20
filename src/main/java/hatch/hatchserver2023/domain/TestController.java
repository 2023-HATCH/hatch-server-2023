package hatch.hatchserver2023.domain;

import hatch.hatchserver2023.domain.user.application.AuthService;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.KakaoDto;
import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.CommonCode;
import hatch.hatchserver2023.global.config.security.jwt.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    private final JwtProvider jwtProvider;

    private final AuthService authService;

    public TestController(JwtProvider jwtProvider, AuthService authService) {
        this.jwtProvider = jwtProvider;
        this.authService = authService;
    }


    @GetMapping("/bd/login")
    // 회원가입&로그인 백도어
    public ResponseEntity<CommonResponse> kakaoSignUpAndLoginBackdoor(@RequestParam Long userNum,
                                                                      HttpServletResponse servletResponse) {
        log.info("[API] GET /api/v1/test/bd/login");
        KakaoDto.GetUserInfo dto = KakaoDto.GetUserInfo.builder()
                .kakaoAccountNumber(userNum)
                .nickname("user_" + userNum)
                .build();
        User user = authService.signUpAndLogin(dto, servletResponse);
        return ResponseEntity.ok().body(CommonResponse.toResponse(CommonCode.CREATED, UserResponseDto.KakaoLogin.toDto(user)));
    }




    @GetMapping
    public ResponseEntity<CommonResponse> test() {
        log.info("[API] GET /api/v1/test");
        return ResponseEntity.ok()
                .body(CommonResponse.toResponse(CommonCode.OK, "test"));
    }



    //// 접근 권한 ROLE 테스트용 ////

    @PreAuthorize("hasAnyRole('ROLE_ANONYMOUS')")
    @GetMapping("/auth/anonymous")
    public ResponseEntity<CommonResponse> authAnonymous(@AuthenticationPrincipal Object principal) {
        log.info("[API] GET /api/v1/test/auth/anonymous");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok()
                .body(CommonResponse.toResponse(CommonCode.OK, "authAnonymous : "+authentication.toString()));
    }

    //    @Secured("ROLE_USER")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping("/auth/user")
    public ResponseEntity<CommonResponse> authUser(@AuthenticationPrincipal Object principal) {
        log.info("[API] GET /api/v1/test/auth/user");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok()
                .body(CommonResponse.toResponse(CommonCode.OK, "authUser : "+authentication.toString()));
    }
}
