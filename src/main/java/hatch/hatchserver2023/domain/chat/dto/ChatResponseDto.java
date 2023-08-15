package hatch.hatchserver2023.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import hatch.hatchserver2023.domain.chat.domain.ChatMessage;
import hatch.hatchserver2023.domain.chat.domain.ChatRoom;
import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class ChatResponseDto {

    @ToString
    @Getter
    @Builder
    public static class CreateChatRoom {
        private UUID chatRoomId;

        public static CreateChatRoom toDto(UUID chatRoomId) {
            return CreateChatRoom.builder()
                    .chatRoomId(chatRoomId)
                    .build();
        }
    }

    @ToString
    @Getter
    @Builder
    public static class GetChatRooms {
        private List<BasicChatRoom> chatRooms;

        public static GetChatRooms toDto(List<ChatModel.ChatRoomInfo> chatRoomInfos) {
            return GetChatRooms.builder()
                    .chatRooms(BasicChatRoom.toDtos(chatRoomInfos))
                    .build();
        }
    }

    @ToString
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BasicChatRoom {
        private UUID chatRoomId;
        private UserResponseDto.SimpleUserProfile opponentUser;
        private String recentContent;
        private String recentSendAt;

        public static BasicChatRoom toDto(ChatModel.ChatRoomInfo chatRoomInfos) {
            ChatRoom chatRoom = chatRoomInfos.getChatRoom();
            return BasicChatRoom.builder()
                    .chatRoomId(chatRoom.getUuid())
                    .recentContent(chatRoom.getRecentContent())
                    .recentSendAt(chatRoom.getRecentSendAtString())
                    .opponentUser(UserResponseDto.SimpleUserProfile.toDto(chatRoomInfos.getOpponentUser()))
                    .build();
        }

        public static List<BasicChatRoom> toDtos(List<ChatModel.ChatRoomInfo> chatRoomInfos) {
            return chatRoomInfos.stream().map(BasicChatRoom::toDto).collect(Collectors.toList());
        }
    }

    @ToString
    @Getter
    @Builder
    public static class GetChatMessages {
        private int page;
        private int size;
        private List<BasicChatMessage> messages;

        public static GetChatMessages toDto(Slice<ChatMessage> chatMessages) {
            return GetChatMessages.builder()
                    .page(chatMessages.getNumber())
                    .size(chatMessages.getSize())
                    .messages(BasicChatMessage.toDtos(chatMessages))
                    .build();
        }
    }

    @ToString
    @Getter
    @Builder
    public static class BasicChatMessage {
        private UUID chatMessageId;
        private String createdAt;
        private UserResponseDto.SimpleUserProfile sender;
        private String content;

        public static BasicChatMessage toDto(ChatMessage chatMessage) {
            return BasicChatMessage.builder()
                    .chatMessageId(chatMessage.getUuid())
                    .createdAt(chatMessage.getCreatedAtString())
                    .sender(UserResponseDto.SimpleUserProfile.toDto(chatMessage.getSender()))
                    .content(chatMessage.getContent())
                    .build();
        }

        public static List<BasicChatMessage> toDtos(Slice<ChatMessage> chatMessages) {
            return chatMessages.stream().map(BasicChatMessage::toDto).collect(Collectors.toList());
        }
    }

    @ToString
    @Getter
    @Builder
    public static class SendChatMessage {
        private String createdAt;
        private UserResponseDto.SimpleUserProfile sender;
        private String content;

        public static SendChatMessage toDto(ChatMessage chatMessage) {
            return SendChatMessage.builder()
                    .createdAt(chatMessage.getCreatedAtString())
                    .sender(UserResponseDto.SimpleUserProfile.toDto(chatMessage.getSender()))
                    .content(chatMessage.getContent())
                    .build();
        }
    }
}
