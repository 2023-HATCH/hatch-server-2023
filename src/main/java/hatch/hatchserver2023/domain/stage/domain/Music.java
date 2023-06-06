package hatch.hatchserver2023.domain.stage.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Music {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name="uuid2", strategy = "uuid2")
    private UUID id;

    // 음악 제목
    @Column(length = 50)
    private String title;

    // 가수
    @Column(length = 50)
    private String singer;

    // 음악 URL
    private String music_url;

    // 정답 안무 스켈레톤
    @Lob
    @Column(columnDefinition="BINARY(200000)")
    private Double[][] answer;

    // 음악 길이 (초 단위)
    private int length;

    // 음악 컨셉
    @Column(length = 50)
    private String concept;
}
