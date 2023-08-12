package hatch.hatchserver2023.domain;

import hatch.hatchserver2023.domain.stage.application.StageDataUtil;
import hatch.hatchserver2023.domain.user.application.AuthService;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.KakaoDto;
import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
import hatch.hatchserver2023.domain.video.application.HashtagService;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.CommonCode;
import hatch.hatchserver2023.global.config.redis.RedisDao;
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
    private final HashtagService hashtagService;
    private final StageDataUtil stageDataUtil;

    private final RedisDao redisDao;

    public TestController(JwtProvider jwtProvider, AuthService authService, HashtagService hashtagService, StageDataUtil stageDataUtil, RedisDao redisDao) {
        this.jwtProvider = jwtProvider;
        this.authService = authService;
        this.hashtagService = hashtagService;
        this.stageDataUtil = stageDataUtil;
        this.redisDao = redisDao;
    }


    @GetMapping("/bd/login")
    // 회원가입&로그인 백도어
    public ResponseEntity<CommonResponse> kakaoSignUpAndLoginBackdoor(@RequestParam Long userNum,
                                                                      HttpServletResponse servletResponse) {
        log.info("[API] GET /api/v1/test/bd/login");
        KakaoDto.GetUserInfo dto = KakaoDto.GetUserInfo.builder()
                .kakaoAccountNumber(userNum)
                .nickname("user_" + userNum)
                .profileImg(null)
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

    @GetMapping("/bd/redis-init")
    public ResponseEntity<CommonResponse> redisDataInit() {
        log.info("[API] GET /api/v1/bd/redis-init");
        redisDao.deleteAll();
        return ResponseEntity.ok()
                .body(CommonResponse.toResponse(CommonCode.OK, "redis DB 초기화 완료. 데이터 전체 삭제"));
    }

    @GetMapping("/bd/stage-init")
    public ResponseEntity<CommonResponse> redisStageDataInit() {
        log.info("[API] GET /api/v1/bd/stage-init");
        stageDataUtil.initStage();
        return ResponseEntity.ok()
                .body(CommonResponse.toResponse(CommonCode.OK, "stage 데이터 초기화 완료"));
    }



    //// 접근 권한 ROLE 테스트용 ////

    @PreAuthorize("hasAnyRole('ROLE_ANONYMOUS')")
    @GetMapping("/auth/anonymous")
    public ResponseEntity<CommonResponse> authAnonymous(@AuthenticationPrincipal User principal) { //principal == null
        log.info("[API] GET /api/v1/test/auth/anonymous");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        checkPrincipal(principal);
        return ResponseEntity.ok()
                .body(CommonResponse.toResponse(CommonCode.OK, "authAnonymous : "+authentication.toString()));
    }

    //    @Secured("ROLE_USER")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping("/auth/user")
    public ResponseEntity<CommonResponse> authUser(@AuthenticationPrincipal User principal) { //@AuthenticationPrincipal 어노테이션으로 로그인된 User 객체 받아올 수 있음
        log.info("[API] GET /api/v1/test/auth/user");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); //인증정보만 필요하면 이렇게도 가져올 수 있음
        log.info("principal.getNickName: "+principal.getNickname()); //user 객체라서 바로 user 정보에 접근 가능
        checkPrincipal(principal);
        return ResponseEntity.ok()
                .body(CommonResponse.toResponse(CommonCode.OK, "authUser : "+authentication.toString()));
    }

    private void checkPrincipal(Object principal) {
        log.info("principal: "+principal);
        if(principal instanceof User) {
            User user = (User) principal;
            log.info("type is user : "+user.getUuid());
        } else{
            log.info("type is not user");
        }
    }

    //--커뮤니티 간이 api--//
    //해시태그 삭제 - 테스트용
    @DeleteMapping("/tags/{title}")
    public boolean deleteHashtag(@PathVariable String title){
        hashtagService.delete(title);
        return true;
    }

}
