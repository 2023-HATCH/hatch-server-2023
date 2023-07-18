package hatch.hatchserver2023.global.config.security.jwt;

import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import hatch.hatchserver2023.global.common.response.exception.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//TODO : 비회원 사용 테스트
@Slf4j
@RequiredArgsConstructor
//@WebFilter(urlPatterns = {"/api/v1/test/*"})
@WebFilter(urlPatterns = {"/api/v1/users/*", "/api/v1/stage/*", "/api/v1/talks/*", "/api/v1/test/auth/*", "/api/v1/videos/*", "/api/v1/likes/*", "/api/v1/comments/*"}) //ws pub?
public class JwtAuthenticationFilter implements Filter { //OncePerRequestFilter ?
    private final JwtProvider jwtProvider;

    //필터 세팅 확인용 로그 찍기
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("[FILTER] jwtAuthenticationFilter : init");
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("[FILTER] jwtAuthenticationFilter doFilter");

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        Cookie[] cookies = httpServletRequest.getCookies();

        if(cookies == null || cookies.length == 0){
            log.info("[FILTER] jwtAuthenticationFilter : cookieList is empty. Guest user");
        }
        else{
            checkCookies(response, cookies);
        }
        chain.doFilter(request, response);

    }

    private void checkCookies(ServletResponse response, Cookie[] cookies) {
        String accessToken = jwtProvider.getAccessToken(cookies);

        // access 토큰 유효
        if(jwtProvider.isTokenValid(accessToken)) {
            log.info("[FILTER] jwtAuthenticationFilter : access token is valid");
            setSecurityAuthentication(accessToken);
        }
        // access 토큰 유효하지 않음
        else {
            log.info("[FILTER] jwtAuthenticationFilter : access token is not valid");
            String refreshToken = jwtProvider.getRefreshToken(cookies);

            // refresh 토큰 유효 : access, refresh 모두 재발급 (RTR)
            if(jwtProvider.isTokenValid(refreshToken)){
                log.info("[FILTER] jwtAuthenticationFilter : refresh token is valid. Recreate tokens");
                renewalTokenCookies(refreshToken, response);
                setSecurityAuthentication(refreshToken);
            }
            // refresh 토큰 유효하지 않음
            else {
                log.info("[FILTER] jwtAuthenticationFilter : refresh token is not valid. Guest user");
//                throw new AuthException(UserStatusCode.GUEST_USER); //비회원 가능
            }
        }
    }

    private void setSecurityAuthentication(String token) {
        try{
            Authentication authentication = jwtProvider.getAuthentication(token); // 인증정보 가져와서
            log.info("[FILTER] jwtAuthenticationFilter setSecurityAuthentication :   " + authentication);

            SecurityContextHolder.getContext().setAuthentication(authentication); // 시큐리티에 저장

            log.info("[FILTER] jwtAuthenticationFilter setSecurityAuthentication :  SecurityContextHolder Authentication " + SecurityContextHolder.getContext().getAuthentication());
        } catch (AuthException authException) {
            if (authException.getCode() == UserStatusCode.UUID_NOT_FOUND){
                log.info("[FILTER] jwtAuthenticationFilter :  User of this token doesn't exist now. Guest user");
            }
            else{
                throw authException;
            }
        }
    }

    private void renewalTokenCookies(String refreshToken, ServletResponse response) {
        Cookie accessTokenCookie = jwtProvider.renewalAccessTokenCookie(refreshToken);
        Cookie refreshTokenCookie = jwtProvider.renewalRefreshTokenCookie(refreshToken);

        HttpServletResponse servletResponse = (HttpServletResponse) response;

        servletResponse.addCookie(accessTokenCookie);
        servletResponse.addCookie(refreshTokenCookie);
    }
}
