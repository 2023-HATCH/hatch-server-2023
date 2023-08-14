package hatch.hatchserver2023.domain.chat.application;

import hatch.hatchserver2023.domain.chat.domain.ChatRoom;
import hatch.hatchserver2023.domain.chat.domain.UserChatRoom;
import hatch.hatchserver2023.domain.chat.repository.ChatRoomRepository;
import hatch.hatchserver2023.domain.chat.repository.UserChatRoomRepository;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.repository.UserRepository;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import hatch.hatchserver2023.global.common.response.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final UserRepository userRepository;

    public ChatService(ChatRoomRepository chatRoomRepository, UserChatRoomRepository userChatRoomRepository, UserRepository userRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.userChatRoomRepository = userChatRoomRepository;
        this.userRepository = userRepository;
    }

    @Transactional //TODO : 사용법...
    public UUID createChatRoom(User user, UUID opponentUserId) {
        User opponentUser = userRepository.findByUuid(opponentUserId)
                .orElseThrow(() -> new UserException(UserStatusCode.UUID_NOT_FOUND));

        ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.builder().build());

        saveUserChatRoom(user, chatRoom);
        saveUserChatRoom(opponentUser, chatRoom);

        return chatRoom.getUuid();
    }

    private void saveUserChatRoom(User user, ChatRoom chatRoom) {
        UserChatRoom userChatRoom = UserChatRoom.builder()
                .chatRoom(chatRoom)
                .user(user)
                .build();
        userChatRoomRepository.save(userChatRoom);
    }
}
