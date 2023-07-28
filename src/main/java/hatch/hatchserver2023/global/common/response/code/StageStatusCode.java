package hatch.hatchserver2023.global.common.response.code;

import org.springframework.http.HttpStatus;

public enum StageStatusCode implements StatusCode {
    // 2xx
    GET_STAGE_ENTER_SUCCESS(HttpStatus.OK, "2001", "스테이지 입장 성공"), //200
    GET_STAGE_EXIT_SUCCESS(HttpStatus.OK, "2002", "스테이지 퇴장 성공"), //200
    GET_STAGE_ENTER_USER_LIST_SUCCESS(HttpStatus.OK, "2003", "스테이지 내 사용자 목록 조회 성공"), //200

    // 4xx

    // 5xx
    ALREADY_ENTERED_USER(HttpStatus.INTERNAL_SERVER_ERROR, "5001", "스테이지에 이미 입장되어있는 사용자임"), //500

    STAGE_ALREADY_EMPTY(HttpStatus.INTERNAL_SERVER_ERROR, "5002", "입장한 인원수가 0임. 퇴장 불가"), //500
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    StageStatusCode(HttpStatus status, String code, String message){
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
        return StatusCodeDoc.STAGE.getInitialWithConnector() + code;
    }

    @Override
    public String getMessage() {
        return StatusCodeDoc.STAGE.getLabelWithBrackets() + message;
    }
}
