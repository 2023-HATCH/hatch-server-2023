package hatch.hatchserver2023.domain.user.application;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.KakaoDto;
import hatch.hatchserver2023.domain.user.repository.UserRepository;
import hatch.hatchserver2023.global.config.redis.FcmTokenDao;
import hatch.hatchserver2023.global.config.security.jwt.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Transactional(readOnly = true)
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final FcmTokenDao FCMTokenDao;

    public AuthService(UserRepository userRepository, JwtProvider jwtProvider, FcmTokenDao FCMTokenDao) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
        this.FCMTokenDao = FCMTokenDao;
    }

    @Transactional //signUp 을 여기서 사용함 - 나중에 로직 개선하기
    public User signUpAndLogin(@Valid KakaoDto.GetUserInfo userInfo, String fcmToken, HttpServletResponse servletResponse) {
        log.info("[SERVICE] signUpAndLogin");
        Optional<User> userOp = userRepository.findByKakaoAccountNumber(userInfo.getKakaoAccountNumber());

        User user;
        if(userOp.isEmpty()) {
            user = userInfo.toUser();
            user.updateLoginDefaultValues();
            user = signUp(user);
        }
        else{
            user = userOp.get();
        }

        setTokenCookies(user.getUuid(), user.getRoles(), servletResponse);

        FCMTokenDao.saveToken(user, fcmToken);

        return user;
    }

    private User signUp(User user) {
        log.info("[SERVICE] signUp");
        return userRepository.save(user);
    }

    private void setTokenCookies(UUID uuid, List<String> roles, HttpServletResponse servletResponse) {
        log.info("[SERVICE] setTokenCookies");
        Cookie accessTokenCookie = jwtProvider.createAccessTokenCookie(uuid.toString(), roles);
        Cookie refreshTokenCookie = jwtProvider.createRefreshTokenCookie(uuid.toString(), roles);

        servletResponse.addCookie(accessTokenCookie);
        servletResponse.addCookie(refreshTokenCookie);
    }

    public void kakaoLogout(User user, HttpServletResponse servletResponse) {
        log.info("[SERVICE] logout");
        removeTokenCookies(servletResponse);
        FCMTokenDao.deleteToken(user);
    }


    private void removeTokenCookies(HttpServletResponse servletResponse) {
        Cookie accessTokenCookie = jwtProvider.resetAccessTokenCookie();
        Cookie refreshTokenCookie = jwtProvider.resetRefreshTokenCookie();

        servletResponse.addCookie(accessTokenCookie);
        servletResponse.addCookie(refreshTokenCookie);
    }

//    private Optional<User> getExistUser(Long kakaoAccountNumber) {
//        log.info("[SERVICE] getExistUser");
//        return userRepository.findByKakaoAccountNumber(kakaoAccountNumber);
//    }
}
