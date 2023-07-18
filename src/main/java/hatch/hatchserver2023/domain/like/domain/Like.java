package hatch.hatchserver2023.domain.like.domain;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.global.common.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@Table(name = "likes")  //like는 sql 예약어로 사용 X
public class Like extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, length = 36, unique = true)
    @Type(type = "org.hibernate.type.UUIDCharType")     //PathVariable로 UUID를 받기 위해 필요
    private UUID uuid;

    //좋아요 눌린 영상
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false) //다대일 관계에서 FK는 다 쪽에 있는 것. 연관 관계의 주인
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Video videoId;

    //좋아요 누른 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) //다대일 관계에서 FK는 다 쪽에 있는 것. 연관 관계의 주인
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User userId;

    @PrePersist
    public void prePersist() {
        this.uuid = UUID.randomUUID();
        super.prePersist(); //BaseTimeEntity
    }

    //== 생성자 ==//
    @Builder
    private Like(Video videoId, User userId) {
        this.videoId = videoId;
        this.userId = userId;
    }

    //기본생성자
    public Like() {}

}
