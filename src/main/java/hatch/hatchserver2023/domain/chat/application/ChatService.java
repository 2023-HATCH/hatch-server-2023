package hatch.hatchserver2023.domain.chat.application;

import hatch.hatchserver2023.domain.chat.domain.ChatMessage;
import hatch.hatchserver2023.domain.chat.domain.ChatRoom;
import hatch.hatchserver2023.domain.chat.domain.UserChatRoom;
import hatch.hatchserver2023.domain.chat.dto.ChatModel;
import hatch.hatchserver2023.domain.chat.repository.ChatMessageRepository;
import hatch.hatchserver2023.domain.chat.repository.ChatRoomRepository;
import hatch.hatchserver2023.domain.chat.repository.UserChatRoomRepository;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.repository.UserRepository;
import hatch.hatchserver2023.global.common.response.code.ChatStatusCode;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import hatch.hatchserver2023.global.common.response.exception.ChatException;
import hatch.hatchserver2023.global.common.response.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public ChatService(ChatRoomRepository chatRoomRepository, UserChatRoomRepository userChatRoomRepository, ChatMessageRepository chatMessageRepository, UserRepository userRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.userChatRoomRepository = userChatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    @Transactional //TODO : 사용법...
    public UUID createChatRoom(User user, UUID opponentUserId) {
        log.info("[SERVICE] createChatRoom");

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

    public List<ChatModel.ChatRoomInfo> getChatRoomInfos(User user) {
        log.info("[SERVICE] getChatRoomInfos");
        List<UserChatRoom> userChatRooms = userChatRoomRepository.findAllByUser(user);

        return ChatModel.ChatRoomInfo.toModels(userChatRooms); //fetch lazy 에러 안생기나?

//        List<ChatRoom> chatRooms = chatRoomRepository.findAllByUser(user);
//
//        List<ChatModel.ChatRoomInfo> chatRoomInfos = new ArrayList<>();
//        for (ChatRoom chatRoom : chatRooms) {
//
//        }
    }

    public Slice<ChatMessage> getChatMessages(UUID chatRoomId, Integer page, Integer size) {
        log.info("[SERVICE] getChatMessages");
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        Pageable pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return chatMessageRepository.findAllByChatRoom(chatRoom, pageRequest);
    }


    public ChatMessage sendChatMessage(UUID chatRoomId, String content, User user) {
        log.info("[SERVICE] sendChatMessage");
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        ChatMessage chatMessage = ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .sender(user)
                        .content(content)
                        .build();

        return chatMessageRepository.save(chatMessage);
    }

    private ChatRoom getChatRoom(UUID chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findByUuid(chatRoomId)
                .orElseThrow(() -> new ChatException(ChatStatusCode.CHAT_ROOM_UUID_NOT_FOUND));
        return chatRoom;
    }
}
