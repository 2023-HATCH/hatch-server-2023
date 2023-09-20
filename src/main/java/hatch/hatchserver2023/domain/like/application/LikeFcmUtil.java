package hatch.hatchserver2023.domain.like.application;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import hatch.hatchserver2023.global.common.response.exception.AuthException;
import hatch.hatchserver2023.global.config.firebase.FcmNotificationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class LikeFcmUtil {

//    private static final String KEY_ADD_LIKE_ID = "likeId";
    private static final String KEY_VIDEO_OF_ADD_LIKE_ID= "videoId";

    private final FcmNotificationUtil fcmNotificationUtil;

    public LikeFcmUtil(FcmNotificationUtil fcmNotificationUtil) {
        this.fcmNotificationUtil = fcmNotificationUtil;
    }

    //푸시 알림 로직
    public void sendAddLikeNotification(User receiver, User likedUser, UUID videoId){
        log.info("[FCM] sendAddLikeNotification");

        // FCM 토큰이 존재하는 사용자인지 확인
        try {
            fcmNotificationUtil.checkFcmTokenExist(receiver);
        } catch (AuthException e) {
            if(e.getCode() == UserStatusCode.FCM_NOTIFICATION_TOKEN_NOT_FOUND){
                log.info("이 사용자의 FCM 토큰이 존재하지 않아 알림을 전송할 수 없음");
                return; // 알림 전송하지 않고 종료
            }
        }

        // FCM 토큰 가져와서 알림 요청 내용(Message) 생성
        String token = fcmNotificationUtil.getFcmToken(receiver);
        Message message = createMessage(token, likedUser, videoId);

        // 알림 전송 요청
        fcmNotificationUtil.send(message);
    }


    //알림 요청 내용(Message)를 생성
    private Message createMessage(String token, User likedUser, UUID videoId){
        //푸시알림 객체 생성
        Notification notification = Notification.builder()
                .setTitle("❤️ " + likedUser.getNickname())  //푸시알림의 제목
                .setBody("회원님 영상에 좋아요를 남겼습니다.")  //푸시알림의 내용
                .build();

        //요청 내용 생성 : 알림 받을 사용자의 토큰, 푸시알림 객체, 푸시알림에 보여질 내용 외에 전달해야하는 데이터들을 key-value 형태로 담아 생성
        return Message.builder()
                .setToken(token)
                .setNotification(notification)
                .putData(FcmNotificationUtil.DATA_KEY_TYPE, FcmNotificationUtil.TYPE_ADD_LIKE)   //푸시 알림의 type
                .putData(KEY_VIDEO_OF_ADD_LIKE_ID, String.valueOf(videoId)) //푸시알림 시 전달해야하는 데이터 1
                .build();
    }
}
