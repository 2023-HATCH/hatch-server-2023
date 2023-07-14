package hatch.hatchserver2023.domain.video.domain;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String title;


    //== 생성자 ==//
    // builder 생성자
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
                .build();
    }
}
