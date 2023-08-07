package hatch.hatchserver2023.domain.stage.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Music {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, length = 36, unique = true)
    @Type(type = "org.hibernate.type.UUIDCharType")     //PathVariable로 UUID를 받기 위해 필요
    private UUID uuid;

    // 음악 제목
    @NotBlank
    @Column(length = 50, nullable = false)
    private String title;

    // 가수
    @Column(length = 50)
    private String singer;

    // 음악 URL
    @Column(name="music_url")
    private String musicUrl;

    // 정답 안무 스켈레톤
    @Lob
    // ALTER TABLE music MODIFY answer mediumblob;
    private Float[][] answer;

    // 음악 길이 (초 단위)
    @Column(nullable = true)
    private int length;

    // 음악 컨셉
    @Column(length = 50)
    private String concept;


    @PrePersist
    public void prePersist() {
        this.uuid = UUID.randomUUID();
    }
}
