package hatch.hatchserver2023.global.common.response.socket;

import hatch.hatchserver2023.global.common.response.code.StatusCode;
import org.springframework.http.HttpStatus;

public enum SocketResponseType implements StatusCode {
    TALK_MESSAGE( "TALK_MESSAGE", "라이브톡 메세지 전송"),
    TALK_REACTION( "TALK_REACTION", "라이브톡 반응 전송"),

    USER_COUNT( "USER_COUNT", "스테이지 인원수"),

    CATCH_START( "CATCH_START", "스테이지 캐치 시작"),
    CATCH_END( "CATCH_END", "스테이지 캐치 끝"),
    CATCH_END_RESTART( "CATCH_END_RESTART", "스테이지 캐치 끝, 캐치 다시 시작"),
    PLAY_START( "PLAY_START", "스테이지 플레이 시작"),
    PLAY_SKELETON( "PLAY_SKELETON", "스테이지 플레이 스켈레톤 전달"),
    PLAY_END( "PLAY_END", "스테이지 플레이 끝"),
    MVP_START( "MVP_START", "스테이지 MVP세리머니 시작"),
    MVP_SKELETON( "MVP_SKELETON", "스테이지 MVP세리머니 스켈레톤 전달"),
    MVP_END( "MVP_END", "스테이지 MVP세리머니 끝"),

    STAGE_ROUTINE_STOP( "STAGE_ROUTINE_STOP", "스테이지 진행 멈춤"),
    ;

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
