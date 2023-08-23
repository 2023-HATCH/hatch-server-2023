package hatch.hatchserver2023.domain.chat.application;

import hatch.hatchserver2023.domain.chat.domain.ChatMessage;
import hatch.hatchserver2023.domain.chat.domain.ChatRoom;
import hatch.hatchserver2023.domain.chat.domain.UserChatRoom;
import hatch.hatchserver2023.domain.chat.dto.ChatModel;
import hatch.hatchserver2023.domain.chat.repository.ChatMessageRepository;
import hatch.hatchserver2023.domain.chat.repository.ChatRoomRepository;
import hatch.hatchserver2023.domain.chat.repository.UserChatRoomRepository;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.code.ChatStatusCode;
import hatch.hatchserver2023.global.common.response.exception.ChatException;
import hatch.hatchserver2023.global.common.response.exception.DefaultException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Transactional(readOnly = true)
@Service
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatService(ChatRoomRepository chatRoomRepository, UserChatRoomRepository userChatRoomRepository, ChatMessageRepository chatMessageRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.userChatRoomRepository = userChatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Transactional(rollbackFor = ChatException.class) // 로직상 발생시킨 에러 발생 시 롤백시킬 것
    public ChatModel.EnterChatRoom enterChatRoom(User user, User opponentUser, Integer size) {
        log.info("[SERVICE] createChatRoom");

        if(user.getId().equals(opponentUser.getId())){
            throw new ChatException(ChatStatusCode.CANNOT_CHAT_MYSELF);
        }

        Optional<ChatRoom> chatRoomOp = getOpponentChatRoom(user, opponentUser);

        if(chatRoomOp.isPresent()) {
            log.info("createChatRoom : already exist");
            UUID chatRoomId = chatRoomOp.get().getUuid();
            Slice<ChatMessage> chatMessages = getChatMessages(chatRoomId, 0, size);
            return ChatModel.EnterChatRoom.toModel(chatRoomId, chatMessages);
        }
        else {
            // 새 채팅방 생성
            log.info("createChatRoom : create new one");
            ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.builder().build());

            saveUserChatRoom(chatRoom, user);
            saveUserChatRoom(chatRoom, opponentUser);

            return ChatModel.EnterChatRoom.toModel(chatRoom.getUuid());
        }
    }

    private void saveUserChatRoom(ChatRoom chatRoom, User user) {
        UserChatRoom userChatRoom = UserChatRoom.builder()
                .chatRoom(chatRoom)
                .user(user)
                .build();
        userChatRoomRepository.save(userChatRoom);
    }

    public List<ChatModel.ChatRoomInfo> getChatRoomInfos(User user) {
        log.info("[SERVICE] getChatRoomInfos");
        List<UserChatRoom> opponentUserChatRooms = getOpponentChatRooms(user);

        return ChatModel.ChatRoomInfo.toModels(opponentUserChatRooms); //fetch lazy 에러 안생김
    }

    @Transactional(rollbackFor = ChatException.class)
    public Slice<ChatMessage> getChatMessages(UUID chatRoomId, Integer page, Integer size) {
        log.info("[SERVICE] getChatMessages");
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        Pageable pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return chatMessageRepository.findAllByChatRoom(chatRoom, pageRequest);
    }



    @Transactional(rollbackFor = ChatException.class)
    public ChatModel.SendChatMessage sendChatMessage(UUID chatRoomId, String content, User user) {
        log.info("[SERVICE] sendChatMessage");
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        // 이 채팅방의 상대 유저 찾기
        User opponentUser = getOpponentUser(chatRoom, user);

        // 채팅 메세지 생성
        ChatMessage chatMessage = ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .sender(user)
                        .content(content)
                        .build();
        ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);

        return ChatModel.SendChatMessage.toModel(savedChatMessage, opponentUser);
    }

    private User getOpponentUser(ChatRoom chatRoom, User user) {
        UserChatRoom opponentUserChatRoom = userChatRoomRepository.findByChatRoomNotMeOne(chatRoom.getId(), user.getId());
        return opponentUserChatRoom.getUser();
    }


    /**
     * 이 사용자와 이미 채팅방이 존재하는지 확인하고 존재한다면 태팅방을 반환하는 메서드
     * @param user
     * @param opponentUser
     * @return
     */
    private Optional<ChatRoom> getOpponentChatRoom(User user, User opponentUser) {
        List<UserChatRoom> opponentUserChatRooms = getOpponentChatRooms(user);

        for(UserChatRoom userChatRoom : opponentUserChatRooms) {
            if(userChatRoom.getUser().getId().equals(opponentUser.getId())) {
                return Optional.of(userChatRoom.getChatRoom());
            }
        }
        return Optional.empty();
    }

    /**
     * 이 사용자와 함께 채팅방이 만들어져 있는 유저들의 UserChatRoom 목록을 반환하는 메서드
     * @param user
     * @return
     */
    private List<UserChatRoom> getOpponentChatRooms(User user) {
        List<UserChatRoom> myUserChatRooms = userChatRoomRepository.findAllByUser(user); //내가 참여중인 채팅방 목록 가져옴

        // 채팅방 참여 정보 중 내가 참여중인 채팅방이면서 참여자가 내가 아닌 채팅방 목록을 가녀옴
        List<UserChatRoom> userChatRooms = new ArrayList<>();
        for(UserChatRoom myUserChatRoom : myUserChatRooms) {
            ChatRoom myChatRoom = myUserChatRoom.getChatRoom();
            UserChatRoom userChatRoom = userChatRoomRepository.findByChatRoomNotMeOne(myChatRoom.getId(), user.getId());
            if(userChatRoom != null) { // 현재 로직상 한쪽이 채팅방을 나갈 수 없으므로 항상 not null 인 게 맞긴 하지만 확인차..
                userChatRooms.add(userChatRoom);
            }
        }
        return userChatRooms;
    }

    private ChatRoom getChatRoom(UUID chatRoomId) {
        return chatRoomRepository.findByUuid(chatRoomId)
                .orElseThrow(() -> new ChatException(ChatStatusCode.CHAT_ROOM_UUID_NOT_FOUND));
    }
}
