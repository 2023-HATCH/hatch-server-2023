package hatch.hatchserver2023.domain.stage.api;

import hatch.hatchserver2023.domain.stage.StageModel;
import hatch.hatchserver2023.domain.stage.domain.Music;
import hatch.hatchserver2023.domain.stage.dto.StageSocketResponseDto;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.socket.SocketResponseType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

//@Slf4j
@Component
public class StageSocketResponser {
    private final String STAGE_WS_SEND_URL = "/topic/stage";

    private final SimpMessagingTemplate simpMessagingTemplate;

    public StageSocketResponser(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void userCount(int userCount) {
        StageSocketResponseDto.UserCount dto = StageSocketResponseDto.UserCount.builder().userCount(userCount).build();
        sendToStage(SocketResponseType.USER_COUNT, dto);
    }

    public void stageRoutineStop() {
        sendToStage(SocketResponseType.STAGE_ROUTINE_STOP);
    }

    public void startCatch(Music music) {
        StageSocketResponseDto.CatchStart dto = StageSocketResponseDto.CatchStart.toDto(music);
        sendToStage(SocketResponseType.CATCH_START, dto);
    }

    public void endCatch(List<User> users) {
        List<StageSocketResponseDto.Player> players = StageSocketResponseDto.Player.toDtos(users);
        sendToStage(SocketResponseType.CATCH_END, StageSocketResponseDto.CatchEnd.toDto(players));
    }

    public void endCatch() {
        sendToStage(SocketResponseType.CATCH_END_RESTART);
    }

    public void startPlay() {
        sendToStage(SocketResponseType.PLAY_START);
    }

    public void endPlay() {
        sendToStage(SocketResponseType.PLAY_END);
    }

    public void startMVP(int mvpPlayerNum, List<StageModel.PlayerResultInfo> playerResultInfos) { //int mvpPlayerNum, List<UserResponseDto.SimpleUserProfile> users
        sendToStage(SocketResponseType.MVP_START, StageSocketResponseDto.PlayResult.toDto(mvpPlayerNum, playerResultInfos));
    }

    public void endMvp() {
        sendToStage(SocketResponseType.MVP_END);
    }


    private void sendToStage(SocketResponseType type) {
        sendToStage(type, null);
    }

    private void sendToStage(SocketResponseType type, Object data) {
        simpMessagingTemplate.convertAndSend(STAGE_WS_SEND_URL, CommonResponse.toSocketResponse(type, data));
    }

}
