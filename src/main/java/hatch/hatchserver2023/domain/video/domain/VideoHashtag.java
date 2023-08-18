package hatch.hatchserver2023.domain.video.domain;

import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "video_hashtag")
public class VideoHashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "video_id", nullable = false) //다대일 관계에서 FK는 다 쪽에 있는 것. 연관 관계의 주인
    private Video video;

    //작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "hashtag_id", nullable = false) //다대일 관계에서 FK는 다 쪽에 있는 것. 연관 관계의 주인
    private Hashtag hashtag;

    //== 생성자 ==//
    @Builder
    private VideoHashtag(Video video, Hashtag hashtag) {
        this.video = video;
        this.hashtag = hashtag;
    }

    //기본생성자
    public VideoHashtag() {}
}
