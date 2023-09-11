package hatch.hatchserver2023.domain.stage.dto;

import hatch.hatchserver2023.domain.stage.domain.Music;
import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
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



    @ToString
    @Getter
    @Builder
    public static class PlayerResultInfo {
        private int playerNum;
        private float similarity;
        private UserResponseDto.SimpleUserProfile player;
        private int usedUserFrameCount;
        private int usedAnswerFrameCount;

        public static PlayerResultInfo toDto(int playerNum, float similarity, UserResponseDto.SimpleUserProfile player, int usedUserFrameCount, int usedAnswerFrameCount) {
            return PlayerResultInfo.builder()
                    .playerNum(playerNum)
                    .similarity(similarity)
                    .player(player)
                    .usedUserFrameCount(usedUserFrameCount)
                    .usedAnswerFrameCount(usedAnswerFrameCount)
                    .build();
        }

//        public static List<PlayerProfile> toDtos(List<Float> similarities, Map<Integer, UserResponseDto.SimpleUserProfile> players) {
//            for()
//            return
//        }
    }

}
