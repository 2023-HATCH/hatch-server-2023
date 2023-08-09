package hatch.hatchserver2023.domain.stage.dto;

import hatch.hatchserver2023.domain.stage.domain.Music;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

public class MusicResponseDto {

    @Builder
    @Getter
    public static class Play {

        private String title;
        private Integer length;
        private String musicUrl;
        //현재 재생 시각(milliseconds 단위)
        private Integer playTime;

        public static Play toDto(Music music, Integer playTime) {
            return Play.builder()
                    .title(music.getTitle())
                    .length(music.getLength())
                    .musicUrl(music.getMusicUrl())
                    .playTime(playTime)
                    .build();
        }
    }

    @Builder
    @Getter
    public static class BasicInfo {
        private UUID musicId;
        private String title;
        private String singer;
        private Integer length;
        private String musicUrl;
        private String concept;

        public static BasicInfo toDto(Music music) {
            return BasicInfo.builder()
                    .musicId(music.getUuid())
                    .title(music.getTitle())
                    .singer(music.getSinger())
                    .length(music.getLength())
                    .musicUrl(music.getMusicUrl())
                    .concept(music.getConcept())
                    .build();
        }
    }
}
