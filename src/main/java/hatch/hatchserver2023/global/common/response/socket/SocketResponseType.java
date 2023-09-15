package hatch.hatchserver2023.global.common.response.socket;

import hatch.hatchserver2023.global.common.response.code.StatusCode;
import org.springframework.http.HttpStatus;

public enum SocketResponseType implements StatusCode {
    CHAT_MESSAGE("CHAT_MESSAGE", "채팅 메세지 전송"),
    TALK_MESSAGE( "TALK_MESSAGE", "라이브톡 메세지 전송"),
    TALK_REACTION( "TALK_REACTION", "라이브톡 반응 전송"),

    USER_COUNT( "USER_COUNT", "스테이지 인원수"),

    CATCH_START(StageStatusType.CATCH_START.getType(), StageStatusType.CATCH_START.getMessage()),
    CATCH_END(StageStatusType.CATCH_END.getType(), StageStatusType.CATCH_END.getMessage()),
    CATCH_END_RESTART(StageStatusType.CATCH_END_RESTART.getType(), StageStatusType.CATCH_END_RESTART.getMessage()),
    PLAY_START(StageStatusType.PLAY_START.getType(), StageStatusType.PLAY_START.getMessage()),
    PLAY_SKELETON( "PLAY_SKELETON", "스테이지 플레이 스켈레톤 전달"),
    PLAY_END(StageStatusType.PLAY_END.getType(), StageStatusType.PLAY_END.getMessage()),

    MVP_START(StageStatusType.MVP_START.getType(), StageStatusType.MVP_START.getMessage()),
    MVP_SKELETON( "MVP_SKELETON", "스테이지 MVP세리머니 스켈레톤 전달"),
    MVP_END(StageStatusType.MVP_END.getType(), StageStatusType.MVP_END.getMessage()),

    STAGE_EXIT( "STAGE_EXIT", "스테이지 퇴장"),
    STAGE_ROUTINE_STOP( "STAGE_ROUTINE_STOP", "스테이지 진행 멈춤"),
    MID_SCORE("MID_SCORE", "스테이지 플레이 중간점수 전달");

    private final String type;
    private final String message;

    SocketResponseType(String type, String message){
        this.type = type;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    @Override
    public String getMessage() {
        return message;
    }


    // 명세만을 위한 getter
    @Override
    public HttpStatus getStatus() {
        return null;
    }

    @Override
    public String getCode() {
        return getType();
    }


}
