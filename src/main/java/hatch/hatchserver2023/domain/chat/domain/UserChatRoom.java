package hatch.hatchserver2023.domain.chat.domain;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Builder
@Entity
@Table(name = "user_chat_room") // 유저-채팅방 M:N 맵핑테이블
public class UserChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, length = 36, unique = true)
    @Type(type = "org.hibernate.type.UUIDCharType")     //PathVariable로 UUID를 받기 위해 필요
    private UUID uuid;

    @OnDelete(action = OnDeleteAction.CASCADE) // 사용자 탈퇴 시 함께 삭제
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OnDelete(action = OnDeleteAction.CASCADE) // 채팅방 삭제 시 함께 삭제
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    public UserChatRoom() {
    }

    public UserChatRoom(Long id, UUID uuid, User user, ChatRoom chatRoom) {
        this.id = id;
        this.uuid = uuid;
        this.user = user;
        this.chatRoom = chatRoom;
    }


    @Override
    public void prePersist() {
        this.uuid = UUID.randomUUID();
        super.prePersist(); //BaseTimeEntity
    }
}
