package hatch.hatchserver2023.global.config.security;

import hatch.hatchserver2023.domain.user.domain.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;
import java.util.UUID;

public class WithCustomAuthSecurityContextFactory implements WithSecurityContextFactory<WithCustomAuth> {
    @Override
    public SecurityContext createSecurityContext(WithCustomAuth annotation) {
//        String uuid = annotation.uuid();
        String nickname = annotation.nickname();
        String profileImg = annotation.profileImg();
        String role = annotation.role();

        User user = User.builder()
//                .uuid(UUID.fromString(uuid))
                .nickname(nickname)
                .profileImg(profileImg)
                .build();
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, "", List.of(new SimpleGrantedAuthority(role)));

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authToken);
        return context;
    }
}
