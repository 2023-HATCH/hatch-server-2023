package hatch.hatchserver2023.domain.stage.dto;

import hatch.hatchserver2023.domain.stage.domain.Music;
import lombok.Data;


@Data
public class MusicRequestDto {

    // 음악 제목
    private String title;

    // 가수
    private String singer;

    // 음악 URL
    private String musicUrl;

    // 정답 안무 스켈레톤
    private Float[][] answer;

    // 음악 길이 (초 단위)
    private int length;

    // 음악 컨셉
    private String concept;

    public Music toEntity() {
        return Music.builder()
                .title(title)
                .singer(singer)
                .musicUrl(musicUrl)
                .answer(answer)
                .length(length)
                .concept(concept)
                .build();
    }
}
