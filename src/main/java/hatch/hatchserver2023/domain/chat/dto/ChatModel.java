package hatch.hatchserver2023.domain.chat.dto;

import hatch.hatchserver2023.domain.chat.domain.ChatMessage;
import hatch.hatchserver2023.domain.chat.domain.ChatRoom;
import hatch.hatchserver2023.domain.chat.domain.UserChatRoom;
import hatch.hatchserver2023.domain.user.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class ChatModel {

    @ToString
    @Getter
    @Builder
    public static class ChatRoomInfo {
        private ChatRoom chatRoom;
        private User opponentUser;

        public static List<ChatRoomInfo> toModels(List<UserChatRoom> userChatRooms) {
            List<ChatRoomInfo> chatRoomInfos = new ArrayList<>();
            for(UserChatRoom userChatRoom : userChatRooms){
                ChatRoomInfo chatRoomInfo = ChatRoomInfo.builder()
                        .chatRoom(userChatRoom.getChatRoom())
                        .opponentUser(userChatRoom.getUser())
                        .build();

                chatRoomInfos.add(chatRoomInfo);
            }
            return chatRoomInfos;
        }
    }

    @ToString
    @Getter
    @Builder
    public static class EnterChatRoom {
        private UUID chatRoomId;
        private Slice<ChatMessage> chatMessages;

        public static EnterChatRoom toModel(UUID chatRoomId) {
            return EnterChatRoom.builder()
                    .chatRoomId(chatRoomId)
                    .build();
        }
        public static EnterChatRoom toModel(UUID chatRoomId, Slice<ChatMessage> chatMessages) {
            return EnterChatRoom.builder()
                    .chatRoomId(chatRoomId)
                    .chatMessages(chatMessages)
                    .build();
        }
    }


    @ToString
    @Getter
    @Builder
    public static class SendChatMessage {
        private ChatMessage chatMessage;
        private User opponentUser;

        public static SendChatMessage toModel(ChatMessage chatMessage, User opponentUser) {
            return SendChatMessage.builder()
                    .chatMessage(chatMessage)
                    .opponentUser(opponentUser)
                    .build();
        }
    }
}
