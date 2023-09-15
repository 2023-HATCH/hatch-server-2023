package hatch.hatchserver2023.domain.chat.application;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import hatch.hatchserver2023.domain.chat.dto.ChatResponseDto;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import hatch.hatchserver2023.global.common.response.exception.AuthException;
import hatch.hatchserver2023.global.config.firebase.FcmNotificationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class ChatFcmUtil {
    private static final String KEY_SEND_CHAT_MESSAGE_CHAT_ROOM_ID = "chatRoomId";
    private static final String KEY_SEND_CHAT_MESSAGE_NICKNAME = "opponentUserNickname";

    private final FcmNotificationUtil fcmNotificationUtil;

    public ChatFcmUtil(FcmNotificationUtil fcmNotificationUtil) {
        this.fcmNotificationUtil = fcmNotificationUtil;
    }

    /**
     * 채팅 전송 푸시알림 요청 전체 로직
     * @param receiver
     * @param chatRoomId
     * @param requestDto
     */
    public void sendChatMessageNotification(User receiver, UUID chatRoomId, ChatResponseDto.BasicChatMessage requestDto) {
        log.info("[FCM] sendChatMessageNotification");

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
        Message message = createMessage(token, chatRoomId, requestDto);

        // 알림 전송 요청
        fcmNotificationUtil.send(message);
    }

    /**
     * 알림 요청 내용(Message)를 생성
     * @param token
     * @param chatRoomId
     * @param dto
     * @return
     */
    private Message createMessage(String token, UUID chatRoomId, ChatResponseDto.BasicChatMessage dto) {
        // 푸시알림 객체 생성
        Notification notification = Notification.builder()
                .setTitle("💬 " + dto.getSender().getNickname()) // 푸시알림의 제목으로 보여질 내용
                .setBody(dto.getContent()) // 푸시알림의 내용으로 보여질 내용
                .build();

        // 요청 내용 생성 : 알림 받을 사용자의 토큰, 푸시알림 객체, 푸시알림에 보여질 내용 외에 전달해야하는 데이터들을 key-value 형태로 담아 생성
        return Message.builder()
                .setToken(token)
                .setNotification(notification)
                .putData(FcmNotificationUtil.DATA_KEY_TYPE, FcmNotificationUtil.TYPE_SEND_CHAT_MESSAGE) // type : 푸시알림의 공통 속성. key 값은 고정이며 value 에 푸시알림 종류를 명시함
                .putData(KEY_SEND_CHAT_MESSAGE_CHAT_ROOM_ID, String.valueOf(chatRoomId)) // 채팅 메세지 전송 푸시알림 시 전달해야하는 데이터. 이런식으로 상황에 따라 필요한 데이터를 추가해 보낼 수 있음
                .putData(KEY_SEND_CHAT_MESSAGE_NICKNAME, dto.getSender().getNickname())
                .build();
    }
}
