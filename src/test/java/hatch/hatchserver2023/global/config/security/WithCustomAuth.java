package hatch.hatchserver2023.global.config.security;

import hatch.hatchserver2023.domain.user.domain.User;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 테스트 코드 작성 시 @AuthenticationPrincipal 등
 * 시큐리티 인증정보를 사용하는 메서드를 테스트하기 위해
 * 인증정보를 세팅해주는 커스텀 어노테이션 인터페이스
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomAuthSecurityContextFactory.class)
public @interface WithCustomAuth {
//    String uuid();
    String nickname();
    String profileImg();
    String role();
}
