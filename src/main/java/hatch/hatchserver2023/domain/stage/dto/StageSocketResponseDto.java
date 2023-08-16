package hatch.hatchserver2023.domain.stage.dto;

import hatch.hatchserver2023.domain.stage.StageModel;
import hatch.hatchserver2023.domain.stage.domain.Music;
import hatch.hatchserver2023.domain.user.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StageSocketResponseDto {

    @ToString
    @Getter
    @Builder
    public static class UserCount {
        private Integer userCount;
    }

    @ToString
    @Getter
    @Builder
    public static class CatchStart {
        private MusicResponseDto.BasicInfo music;

        public static CatchStart toDto(Music music) {
            return CatchStart.builder()
                    .music(MusicResponseDto.BasicInfo.toDto(music))
                    .build();
        }
    }
    @ToString
    @Getter
    @Builder
    public static class CatchEnd {
        private List<Player> players;

        public static CatchEnd toDto(List<Player> players) {
            return CatchEnd.builder()
                    .players(players)
                    .build();
        }
    }

    @ToString
    @Getter
    @Builder
    public static class Player {
        private Integer playerNum;
        private UUID userId;
        private String nickname;
        private String profileImg;

        public static List<StageSocketResponseDto.Player> toDtos(List<User> users) {
            List<Player> players = new ArrayList<>();
            for(int i=0; i<users.size(); i++) {
                Player player = Player.toDto(users.get(i), i);
                players.add(player);
            }
            return players;
        }

        public static Player toDto(User user, int playerNum) {
            return Player.builder()
                    .playerNum(playerNum)
                    .userId(user.getUuid())
                    .nickname(user.getNickname())
                    .profileImg(user.getProfileImg())
                    .build();
        }
    }

    @ToString
    @Getter
    @Builder
    public static class SendPlaySkeleton {
        private UUID userId;
        private Integer playerNum;
        private Integer frameNum;
        private StageRequestDto.Skeleton skeleton;

        public static SendPlaySkeleton toDto(StageRequestDto.SendPlaySkeleton dto, User user) {
            return SendPlaySkeleton.builder()
                    .userId(user.getUuid())
                    .playerNum(dto.getPlayerNum())
                    .frameNum(dto.getFrameNum())
                    .skeleton(dto.getSkeleton())
                    .build();
        }
    }

//    @ToString
//    @Getter
//    @Builder
//    public static class StartMvp {
//        private UserResponseDto.SimpleUserProfile mvpUser;
//
//        public static StartMvp toDto(UserResponseDto.SimpleUserProfile mvpUser) {
//            return StartMvp.builder()
//                    .mvpUser(mvpUser)
//                    .build();
//        }
//    }



    @ToString
    @Getter
    @Builder
    public static class PlayResult {
        private int mvpPlayerNum;
        private List<StageModel.PlayerResultInfo> playerInfos;

        public static PlayResult toDto(int mvpPlayerNum, List<StageModel.PlayerResultInfo> playerResultInfos) {
            return PlayResult.builder()
                    .mvpPlayerNum(mvpPlayerNum)
                    .playerInfos(playerResultInfos)
                    .build();
        }
    }

    @ToString
    @Getter
    @Builder
    public static class SendMvpSkeleton {
        private UUID userId;
        private Integer frameNum;
        private StageRequestDto.Skeleton skeleton;

        public static SendMvpSkeleton toDto(StageRequestDto.SendMvpSkeleton dto, User user) {
            return SendMvpSkeleton.builder()
                    .userId(user.getUuid())
                    .frameNum(dto.getFrameNum())
                    .skeleton(dto.getSkeleton())
                    .build();
        }
    }
}
