package hatch.hatchserver2023.domain;

import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.CommonCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

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
