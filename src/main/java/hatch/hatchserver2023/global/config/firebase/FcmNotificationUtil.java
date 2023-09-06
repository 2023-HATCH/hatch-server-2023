package hatch.hatchserver2023.global.config.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import hatch.hatchserver2023.domain.chat.dto.ChatResponseDto;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.code.ChatStatusCode;
import hatch.hatchserver2023.global.common.response.exception.ChatException;
import hatch.hatchserver2023.global.config.redis.FcmTokenDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class FcmNotificationUtil {
    private static final String DATA_KEY_SEND_CHAT_MESSAGE_TYPE = "type";
    private static final String DATA_KEY_SEND_CHAT_MESSAGE_CHAT_ROOM_ID = "chatRoomId";

    private static final String FCM_SEND_CHAT_MESSAGE_TYPE = "SEND_CHAT_MESSAGE";

    private final FcmTokenDao fcmTokenDao;

    public FcmNotificationUtil(FcmTokenDao fcmTokenDao) {
        this.fcmTokenDao = fcmTokenDao;
    }

    public void sendChatMessageNotification(User receiver, UUID chatRoomId, ChatResponseDto.BasicChatMessage requestDto) {
        log.info("[FCM] sendChatMessageNotification");

        if(!fcmTokenDao.isTokenExist(receiver)) {
            throw new ChatException(ChatStatusCode.CHAT_NOTIFICATION_TOKEN_NOT_FOUND);
        }

        String token = fcmTokenDao.getToken(receiver);
        Message message = createMessage(token, chatRoomId, requestDto);

        send(message);
    }

    private void send(Message message) {
        log.info("[FCM] send");
        FirebaseMessaging.getInstance().sendAsync(message); //비동기로 알림 요청 처리
    }

    private Message createMessage(String token, UUID chatRoomId, ChatResponseDto.BasicChatMessage dto) {
        Notification notification = Notification.builder()
                .setTitle(dto.getSender().getNickname())
                .setBody(dto.getContent())
                .build();

        return Message.builder()
                .setToken(token)
                .setNotification(notification)
                .putData(DATA_KEY_SEND_CHAT_MESSAGE_TYPE, FCM_SEND_CHAT_MESSAGE_TYPE)
                .putData(DATA_KEY_SEND_CHAT_MESSAGE_CHAT_ROOM_ID, String.valueOf(chatRoomId))
                .build();
    }
}
