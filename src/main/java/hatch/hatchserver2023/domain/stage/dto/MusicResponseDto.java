package hatch.hatchserver2023.domain.stage.dto;

import hatch.hatchserver2023.domain.stage.domain.Music;
import lombok.Builder;
import lombok.Getter;

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
}
