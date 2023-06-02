package hatch.hatchserver2023.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable() //rest api 만을 고려하여 기본 설정을 해제함
                .csrf().disable() //csrf 보안 토큰 disable
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                // STATELESS : 세션이 아닌 토큰 기반 인증을 사용하므로 세션을 사용하지 않음
                .and()
                .authorizeRequests() //요청에 대한 사용권한 체크
//                .antMatchers("/admin/**").hasRole("ROLE_ADMIN")
//                .antMatchers("api/users/**").hasRole("ROLE_USER")
                .antMatchers("/", "/**").permitAll() //그 외 요청들은 누구나 접근 허용
        ;

        http
                .formLogin().disable() //security 기본 로그인 페이지 제거
//                .headers().frameOptions().disable() //TODO : ??
        ;

        // Filter 단에서 발생하는 에러를 잡을 Filter 추가
//        http.addFilterBefore(
//                new ExceptionHandlerFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}