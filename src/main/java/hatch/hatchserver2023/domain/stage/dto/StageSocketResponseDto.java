package hatch.hatchserver2023.domain.stage.dto;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
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
    public static class CatchEnd {
        private List<Player> players;
        private Object music; // TODO

        public static CatchEnd toDto(List<Player> players, Object music) {
            return CatchEnd.builder()
                    .players(players)
                    .music(music)
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

    @ToString
    @Getter
    @Builder
    public static class StartMvp {
        private UserResponseDto.SimpleUserProfile mvpUser;

        public static StartMvp toDto(UserResponseDto.SimpleUserProfile mvpUser) {
            return StartMvp.builder()
                    .mvpUser(mvpUser)
                    .build();
        }
    }

}
