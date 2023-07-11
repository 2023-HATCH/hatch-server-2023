package hatch.hatchserver2023.domain.video.domain;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20, unique = true)
    private String title;

    @ManyToMany
    @JoinTable(name = "video_hashtag")
    private List<Video> videoList = new ArrayList<>();


    //== 생성자 ==//
    // builder 생성자
    @Builder
    private Hashtag(String title, List<Video> videoList){
        this.title = title;
        this.videoList = videoList;
    }

    // 기본 생성자
    public Hashtag() {}


    //== 생성 메서드 ==//
    public static Hashtag createHashtag(String title){
        return Hashtag.builder()
                .title(title)
                .videoList(new ArrayList<>())
                .build();
    }
}
