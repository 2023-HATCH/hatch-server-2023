package hatch.hatchserver2023.domain.video.domain;

import hatch.hatchserver2023.domain.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "video_hashtag")
public class VideoHashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false) //다대일 관계에서 FK는 다 쪽에 있는 것. 연관 관계의 주인
    private Video videoId;

    //작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id", nullable = false) //다대일 관계에서 FK는 다 쪽에 있는 것. 연관 관계의 주인
    private Hashtag hashtagId;

    //== 생성자 ==//
    @Builder
    private VideoHashtag(Video videoId, Hashtag hashtagId) {
        this.videoId = videoId;
        this.hashtagId = hashtagId;
    }

    //기본생성자
    public VideoHashtag() {}
}
