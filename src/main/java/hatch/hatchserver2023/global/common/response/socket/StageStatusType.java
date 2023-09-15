package hatch.hatchserver2023.global.common.response.socket;

import hatch.hatchserver2023.global.common.response.code.StatusCode;
import org.springframework.http.HttpStatus;

public enum StageStatusType implements StatusCode {
    WAIT("WAIT", "스테이지 대기중"),

    CATCH_START( "CATCH_START", "스테이지 캐치 시작"),
    CATCH( "CATCH", "스테이지 캐치중"),
    CATCH_END( "CATCH_END", "스테이지 캐치 끝"),
    CATCH_END_RESTART( "CATCH_END_RESTART", "스테이지 캐치 끝, 캐치 다시 시작"),

    PLAY_START( "PLAY_START", "스테이지 플레이 시작"),
    PLAY( "PLAY", "스테이지 플레이중"),
    PLAY_END( "PLAY_END", "스테이지 플레이 끝"),

    MVP_START( "MVP_START", "스테이지 MVP세리머니 시작"),
    MVP( "MVP", "스테이지 MVP세리머니중"),
    MVP_END( "MVP_END", "스테이지 MVP세리머니 끝"),
    ;

    private final String type;
    private final String message;

    StageStatusType(String type, String message){
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
