package hatch.hatchserver2023.domain.video.domain;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
public class Video extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, insertable = false, updatable = false, length = 36, unique = true)
    @ColumnDefault("(uuid())")
    private UUID uuid;

    //작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")    //다대일 관계에서 FK는 다 쪽에 있는 것. 연관 관계의 주인
    @Column(name="user_id", nullable = false)
    private User userId;

    //제목
    @Column(nullable = false, length = 50)
    private String title;

    //해시태그
    @Column(length = 100)
    private String tag;

    //영상 URL
    @Column(name = "video_url", nullable = false)
    private String videoUrl;

    //썸네일
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    //영상 길이 (milliseconds 단위)
    private int length;

    //좋아요 수
    @Column(name = "like_count", nullable = false)
    @ColumnDefault("0")
    private int likeCount;

    // 댓글 수
    @Column(name = "comment_count", nullable = false)
    @ColumnDefault("0")
    private int commentCount;

    //조회수 (미정)
    @Column(name = "view_count", nullable = false)
    @ColumnDefault("0")
    private int viewCount;



    // builder 생성자 (동작하겠지?)
    @Builder
    private Video(User userId, String title, String tag, String videoUrl, String thumbnailUrl, int length, int viewCount, int likeCount, int commentCount) {
        this.userId = userId;
        this.title = title;
        this.tag = tag;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.length = length;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
    }


    //==비즈니스 메서드==//

    /**
     * 좋아요 수 증가
     */
    public void addLikeCount() { this.likeCount += 1; }

    /**
     * 댓글 수 증가
     */
    public void addCommentCount() { this.commentCount += 1; }

    /**
     * 조회수 증가 (Todo: 여기있어서 사용할 수 있을지는 미지수)
     * @param views
     */
    public void addViewCount(int views) { this.viewCount += views; }


    //==생성 메서드==//
//    @Builder
//    public static Video createVideo() {
//
//    }

}