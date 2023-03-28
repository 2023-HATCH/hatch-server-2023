package hatch.hatchserver2023.global.common.response.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommonCode implements StatusCode {
    OK(HttpStatus.OK, "C200", "정상 처리"), //200
    CREATED(HttpStatus.CREATED, "C201", "새로운 리소스 생성"), //201
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "C400", "잘못된 요청"), //400
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C401", "인증되지 않음"), //401
    FORBIDDEN(HttpStatus.FORBIDDEN, "C403", "권한 부족"), //403
    NOT_FOUND(HttpStatus.NOT_FOUND, "C404", "요청 리소스를 찾을 수 없음"), //404
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C500", "서버 내부 오류"); //500

    private HttpStatus status;
    private String code;
    private String message;

    CommonCode(HttpStatus status, String code, String message){
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
