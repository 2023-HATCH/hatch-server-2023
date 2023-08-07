package hatch.hatchserver2023.domain.stage.api;

import hatch.hatchserver2023.domain.stage.dto.StageSocketResponseDto;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.socket.SocketResponseType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public void startCatch(String tempData) {
        sendToStage(SocketResponseType.CATCH_START, tempData);
    }

    public void endCatch(List<User> users) {
        List<StageSocketResponseDto.Player> players = StageSocketResponseDto.Player.toDtos(users);
        sendToStage(SocketResponseType.CATCH_END, StageSocketResponseDto.CatchEnd.toDto(players, "개발중"));
    }

    public void endCatch() {
        sendToStage(SocketResponseType.CATCH_END_RESTART);
    }

    public void startPlay(String tempData) {
        sendToStage(SocketResponseType.PLAY_START, tempData);
    }

    public void endPlay() {
        sendToStage(SocketResponseType.PLAY_END);
    }

    public void startMVP(UserResponseDto.SimpleUserProfile mvpUSer) {
        sendToStage(SocketResponseType.MVP_START, StageSocketResponseDto.StartMvp.toDto(mvpUSer));
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
