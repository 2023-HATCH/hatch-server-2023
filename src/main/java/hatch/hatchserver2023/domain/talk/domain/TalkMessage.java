package hatch.hatchserver2023.domain.talk.domain;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Builder
@Entity
@Table(name = "talk_message")
public class TalkMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "uuid-char")
    @Column(nullable = false, length = 36, unique = true) // uuid 값이 36자의 문자열로 저장됨
    private UUID uuid;

    @Column(nullable = false, length = 200)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // uuid prepersist (auto generate) + BaseTimeEntity prePersist
    @Override
    public void prePersist() {
        this.uuid = UUID.randomUUID(); //TODO : UUID2 전략 적용하기
        super.prePersist(); //BaseTimeEntity
    }

    public TalkMessage(Long id, UUID uuid, String content, User user) {
        this.id = id;
        this.uuid = uuid;
        this.content = content;
        this.user = user;
    }

    public void updateUser(User user) {
        this.user = user;
    }

    public TalkMessage() {

    }
}
