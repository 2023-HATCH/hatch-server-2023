package hatch.hatchserver2023.global.config.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import hatch.hatchserver2023.global.common.response.exception.AuthException;
import hatch.hatchserver2023.global.config.redis.FcmTokenDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FcmNotificationUtil {
    public static final String DATA_KEY_TYPE = "type";

    // 푸시 알림 유형. 이곳에 상수값을 추가해서 사용할 것. key 값이 type 인 데이터의 value 값으로 사용됨
    public static final String TYPE_SEND_CHAT_MESSAGE = "SEND_CHAT_MESSAGE";
    public static final String TYPE_ADD_COMMENT = "ADD_COMMENT";
    public static final String TYPE_ADD_LIKE = "ADD_LIKE";

    private final FcmTokenDao fcmTokenDao;

    public FcmNotificationUtil(FcmTokenDao fcmTokenDao) {
        this.fcmTokenDao = fcmTokenDao;
    }


    /**
     * 이 사용자의 FCM 토큰 존재 여부 확인 (redis)
     * @param receiver
     */
    public void checkFcmTokenExist(User receiver) {
        if(!fcmTokenDao.isTokenExist(receiver)) {
            throw new AuthException(UserStatusCode.FCM_NOTIFICATION_TOKEN_NOT_FOUND);
        }
    }

    /**
     * FCM 토큰 조회 (redis)
     * @param receiver
     * @return
     */
    public String getFcmToken(User receiver) {
        return fcmTokenDao.getToken(receiver);
    }

    /**
     * 알림 전송 요청
     * @param message
     */
    public void send(Message message) {
        log.info("[FCM] send");
        FirebaseMessaging.getInstance().sendAsync(message); //비동기로 알림 요청 처리
    }

}
