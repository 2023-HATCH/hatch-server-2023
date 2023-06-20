package hatch.hatchserver2023.global.config;

import hatch.hatchserver2023.global.common.response.exception.FilterExceptionHandlerFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true) // 컨트롤러 메서드에서 어노테이션으로 권한 체크 허용
@Configuration //WebSecurityConfigurerAdapter 가 deprecated 되어 @EnableWebSecurity 도 사용하지 않게 됨
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable() //rest api 만을 고려하여 기본 설정을 해제함
                .csrf().disable() //csrf 보안 토큰 disable
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests() //요청에 대한 사용권한 체크
//                .antMatchers("/").hasRole("USER")
//                .antMatchers("/api/v1/test/auth/user").hasRole("USER")
//                .antMatchers("/admin/**").hasRole("ADMIN")
//                .antMatchers("api/users/**").hasRole("USER")
//                .antMatchers("/api/v1/auth/login").permitAll()
                .antMatchers("/**").permitAll() //그 외 요청들은 누구나 접근 허용
        //자세한 경로를 먼저 적을 것!
        ;

        http
                .formLogin().disable() //security 기본 로그인 페이지 제거
        ;

        // Filter 단에서 발생하는 에러를 잡을 Filter 추가
        http.addFilterBefore(
                new FilterExceptionHandlerFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}