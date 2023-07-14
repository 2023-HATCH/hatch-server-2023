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

    //https://velog.io/@korea3611/Spring-Boot-%ED%95%B4%EC%8B%9C%ED%83%9C%EA%B7%B8-%EA%B8%B0%EB%8A%A5%EC%9D%84-%EA%B0%80%EC%A7%80%EA%B3%A0-%EC%9E%88%EB%8A%94-%EA%B2%8C%EC%8B%9C%ED%8C%90-%EB%A7%8C%EB%93%A4%EC%96%B4%EB%B3%B4%EA%B8%B0
    //https://velog.io/@yebali/Spring-JPA%EC%9D%98-1N-%EA%B4%80%EA%B3%84-%EB%A7%A4%ED%95%91
    //TODO: 테이블이 새로 만들어지지 않아도 cascade 적용되는건가?
//    @ManyToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
//    @JoinTable(name = "video_hashtag")
//    private List<Video> videoList = new ArrayList<>();


    //== 생성자 ==//
    // builder 생성자
//    @Builder
//    private Hashtag(String title, List<Video> videoList){
//        this.title = title;
//        this.videoList = videoList;
//    }

    @Builder
    private Hashtag(String title){
        this.title = title;
    }

    // 기본 생성자
    public Hashtag() {}


    //== 생성 메서드 ==//
    public static Hashtag createHashtag(String title){
        return Hashtag.builder()
                .title(title)
//                .videoList(new ArrayList<>())
                .build();
    }
}
