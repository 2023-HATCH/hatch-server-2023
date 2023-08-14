package hatch.hatchserver2023.domain.chat.domain;

import hatch.hatchserver2023.global.common.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Builder
@Entity
@Table(name = "chat_room")
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, length = 36, unique = true)
    @Type(type = "org.hibernate.type.UUIDCharType")     //PathVariable로 UUID를 받기 위해 필요
    private UUID uuid;

    @Column(length = 200)
    private String recentContent;

    private ZonedDateTime recentSendTime;

    @Override
    public void prePersist() {
        this.uuid = UUID.randomUUID();
        super.prePersist(); //BaseTimeEntity
    }


}
