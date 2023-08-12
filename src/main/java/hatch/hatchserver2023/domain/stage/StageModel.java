package hatch.hatchserver2023.domain.stage;

import hatch.hatchserver2023.domain.stage.domain.Music;
import hatch.hatchserver2023.domain.stage.dto.MusicResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

public class StageModel {

    @ToString
    @Getter
    @Builder
    public static class StageInfo {
        private String stageStatus;
        private Long statusElapsedTime;
        private Music currentMusic;

//        public static StageModel.StageInfo toModel(String stageStatus, Long statusElapsedTime) {
//            return StageModel.StageInfo.builder()
//                    .stageStatus(stageStatus)
//                    .statusElapsedTime(statusElapsedTime)
//                    .build();
//        }

        public static StageModel.StageInfo toModel(String stageStatus, Long statusElapsedTime, Music music) {
            return StageModel.StageInfo.builder()
                    .stageStatus(stageStatus)
                    .statusElapsedTime(statusElapsedTime)
                    .currentMusic(music)
                    .build();
        }
    }

}