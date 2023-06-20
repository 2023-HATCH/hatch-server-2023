package hatch.hatchserver2023.global.config.security.jwt;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import hatch.hatchserver2023.global.common.response.exception.AuthException;
import hatch.hatchserver2023.global.config.security.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Component
public class JwtProvider {
    private final CustomUserDetailsService customUserDetailsService;

    @Value("${jwt.token.secretKey}")
    private String secretKey;
    public final String ACCESS_TOKEN_NAME = "x-access-token";
    public final String REFRESH_TOKEN_NAME = "x-refresh-token";
    private final String CLAIMS_NAME_ROLES = "roles";

    // access, refresh 토큰 유효기간 : 각 1시간, 30일
    public final int ACCESS_COOKIE_MAX_AGE = 60*60; // 1시간
    public final int REFRESH_COOKIE_MAX_AGE = 30*24*60*60; // 30일
    private final long ACCESS_TOKEN_VALID_TIME = ACCESS_COOKIE_MAX_AGE * 1000L; // 쿠키 유효기간과 동일하게 설정, 나노초로 단위 변경
    private final long REFRESH_TOKEN_VALID_TIME = REFRESH_COOKIE_MAX_AGE * 1000L;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }


    public Cookie createAccessTokenCookie(String userPK, List<String> roles) {
        Cookie cookie = createTokenCookie(userPK, roles, ACCESS_TOKEN_VALID_TIME, ACCESS_TOKEN_NAME);
        cookie.setMaxAge(ACCESS_COOKIE_MAX_AGE);
        return cookie;
    }

    public Cookie createRefreshTokenCookie(String userPK, List<String> roles) {
        Cookie cookie = createTokenCookie(userPK, roles, REFRESH_TOKEN_VALID_TIME, REFRESH_TOKEN_NAME);
        cookie.setMaxAge(REFRESH_COOKIE_MAX_AGE);
        return cookie;
    }

    public Cookie renewalRefreshTokenCookie(String refreshToken){
        String uuid = getUserPK(refreshToken);
        List<String> roles = getRoles(refreshToken);
        return createRefreshTokenCookie(uuid, roles);
    }
    public Cookie renewalAccessTokenCookie(String accessToken){
        String uuid = getUserPK(accessToken);
        List<String> roles = getRoles(accessToken);
        return createAccessTokenCookie(uuid, roles);
    }

    public String getAccessToken(Cookie[] cookies) {
        return getToken(cookies, ACCESS_TOKEN_NAME);
    }

    public String getRefreshToken(Cookie[] cookies) {
        return getToken(cookies, REFRESH_TOKEN_NAME);
    }

    private Cookie createTokenCookie(String userPK, List<String> roles, long validTime, String tokenType) {
        log.info("jwtProvider createTokenCookie");
        String token = createToken(userPK, roles, validTime);
        Cookie cookie = new Cookie(tokenType, token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
//        cookie.setSecure(true); //https 상에서만 동작
        return cookie;
    }

    private String createToken(String userPK, List<String> roles, long validTime) {
        log.info("jwtProvider createToken");

        Claims claims = Jwts.claims().setSubject(userPK);
        claims.put(CLAIMS_NAME_ROLES, roles);
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now) //토큰 발행 시간 저장
                .setExpiration(new Date(now.getTime() + validTime)) //토큰 만료 시각 설정
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    private String getToken(Cookie[] cookies, String tokenType) {
        String token = "";
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(tokenType)) {
                token = cookie.getValue();
                break;
            }
        }

        if(token == null){
            log.info("jwtProvider getToken : token is empty");
            return ""; //어차피 isTokenValid 에서 잡힐거고 그게 더 코드가 깔끔함
//            throw new AuthException(AuthErrorCode.TOKEN_IS_EMPTY); //비회원?
        }
        else{
            log.info("jwtProvider getToken {} : {}", tokenType, token);
            return token;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = getClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e){
            return false; //유효하지 않음 (setSigningKey 에서 파싱 시 나는 에러 캐치. 토큰이 내 키로 해독이 안되는 거였나?)
        }
    }

    public Authentication getAuthentication(String token) throws AuthException {
        String uuid = getUserPK(token);
        User user = getUser(uuid);
        log.info("jwtProvider getAuthentication :  user " + user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUuid(), "", user.getAuthorities());  //principal에 uuid, authorities 설정
        return authentication;
    }

    private String getUserPK(String token){
        String userPK;
        try {
            // TODO : 만료된 토큰을 주면 왜 여기서 에러를 던질까
            Claims claims = getClaims(token);
            userPK = claims.getSubject(); //setSubject했던 값 가져오기
        } catch (Exception e) {
            log.info("jwtProvider getUserPKByToken : {}", e.getMessage());
            e.printStackTrace();
            throw new AuthException(UserStatusCode.TOKEN_CANNOT_RESOLVE); //토큰에서 회원 정보를 확인할 수 없을 때 throw
        }
        log.info("jwtProvider getUserPKByToken userPK : {}", userPK);
        return userPK;
    }

    private List<String> getRoles(String token){
        List<String> roles;
        try {
            // TODO : 만료된 토큰을 주면 왜 여기서 에러를 던질까
            Claims claims = getClaims(token);
            roles = (List<String>) claims.get(CLAIMS_NAME_ROLES); //put 했던 값 가져오기
        } catch (Exception e) {
            log.info("jwtProvider getRoles : {}", e.getMessage());
            e.printStackTrace();
            throw new AuthException(UserStatusCode.TOKEN_CANNOT_RESOLVE); //토큰에서 회원 정보를 확인할 수 없을 때 throw
        }
        log.info("jwtProvider getRoles roles : {}", roles);
        return roles;
    }

    private Claims getClaims(String token) throws Exception {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    private User getUser(String uuid) throws AuthException {
        try{
            return (User) customUserDetailsService.loadUserByUsername(uuid);
        } catch (AuthException authException) {
            if (authException.getCode() == UserStatusCode.UUID_NOT_FOUND){
                log.info("jwtProvider :  User of this token doesn't exist now");
            }
            throw authException;
        }
    }
}
