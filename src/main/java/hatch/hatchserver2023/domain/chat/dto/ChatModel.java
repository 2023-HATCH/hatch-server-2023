package hatch.hatchserver2023.domain.chat.dto;

import hatch.hatchserver2023.domain.chat.domain.ChatRoom;
import hatch.hatchserver2023.domain.chat.domain.UserChatRoom;
import hatch.hatchserver2023.domain.user.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
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
}
