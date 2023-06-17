package hatch.hatchserver2023.global.common.response.code;

import org.springframework.http.HttpStatus;

//@Getter
public enum CommonCode implements StatusCode {
    OK(HttpStatus.OK, "200", "정상 처리"), //200
    CREATED(HttpStatus.CREATED, "201", "새로운 리소스 생성"), //201
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "400", "잘못된 요청"), //400
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "401", "인증되지 않음"), //401
    FORBIDDEN(HttpStatus.FORBIDDEN, "403", "권한 부족"), //403
    NOT_FOUND(HttpStatus.NOT_FOUND, "404", "요청 리소스를 찾을 수 없음"), //404
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500", "서버 내부 오류"); //500

    private final HttpStatus status;
    private final String code;
    private final String message;

    CommonCode(HttpStatus status, String code, String message){
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
        return StatusCodeDoc.COMMON.getInitialWithConnector() + code;
    }

    @Override
    public String getMessage() {
        return StatusCodeDoc.COMMON.getLabelWithBrackets() + message;
    }
}
