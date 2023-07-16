package hatch.hatchserver2023.domain.video.domain;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, length = 36, unique = true)
    @Type(type = "org.hibernate.type.UUIDCharType")     //PathVariable로 UUID를 받기 위해 필요
    private UUID uuid;

    //영상 식별자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false) //다대일 관계에서 FK는 다 쪽에 있는 것. 연관 관계의 주인
    private Video videoId;

    //작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) //다대일 관계에서 FK는 다 쪽에 있는 것. 연관 관계의 주인
    private User userId;

    //내용
    @Column(length = 200)
    private String content;


    @PrePersist
    public void prePersist() {
        this.uuid = UUID.randomUUID();
        super.prePersist(); //BaseTimeEntity
    }

    //== 생성자 ==//
    @Builder
    private Comment(UUID uuid, Video videoId, User userId, String content) {
        this.uuid = uuid;
        this.videoId = videoId;
        this.userId = userId;
        this.content = content;
    }

    //기본생성자
    public Comment() {}


}
