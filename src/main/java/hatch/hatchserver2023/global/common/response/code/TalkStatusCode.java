package hatch.hatchserver2023.global.common.response.code;

import org.springframework.http.HttpStatus;

public enum TalkStatusCode implements StatusCode {
    // 2xx
    GET_TALK_MESSAGES_SUCCESS(HttpStatus.OK, "2001", "라이브톡 메세지 목록 조회 성공"), //200

    // 4xx

    // 5xx
    CREATED_TIME_NULL(HttpStatus.INTERNAL_SERVER_ERROR, "5001", "라이브톡 메세지의 생성 시각값이 존재하지 않음"), //500

    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    TalkStatusCode(HttpStatus status, String code, String message){
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return StatusCodeDoc.TALK.getInitialWithConnector() + code;
    }

    @Override
    public String getMessage() {
        return StatusCodeDoc.TALK.getLabelWithBrackets() + message;
    }
}
