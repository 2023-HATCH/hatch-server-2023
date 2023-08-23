package hatch.hatchserver2023.global.config.redis;

import hatch.hatchserver2023.domain.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class FcmTokenDao {
    private static final String KEY_FCM_TOKEN = "user:fcm:token:";
    private final RedisDao redisDao;

    public FcmTokenDao(RedisDao redisDao) {
        this.redisDao = redisDao;
    }

    public void saveToken(User user, String fcmToken) {
        redisDao.setValues(toFCMTokenKey(user), fcmToken);
    }

    public String getToken(User user) {
        return redisDao.getValues(toFCMTokenKey(user));
    }

    public void deleteToken(User user) {
        redisDao.deleteValues(toFCMTokenKey(user));
    }

    public boolean isTokenExist(User user) {
        return redisDao.isKeyExist(toFCMTokenKey(user));
    }

    private String toFCMTokenKey(User user) {
        return KEY_FCM_TOKEN + user.getId();
    }
}
