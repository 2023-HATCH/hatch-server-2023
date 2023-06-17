package hatch.hatchserver2023.global.common.response.code;

import org.springframework.http.HttpStatus;

//@Getter
public enum UserStatusCode implements StatusCode {

    LOGIN_SUCCESS(HttpStatus.OK, "2000", "로그인 성공"), //201
    SIGN_UP_SUCCESS(HttpStatus.CREATED, "2001", "회원가입 성공"), //201
    LOGIN_ID_NOT_EXIST(HttpStatus.BAD_REQUEST, "4004", "로그인 아이디 존재하지 않음"), //400
//    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "401", "인증되지 않음"), //401
//    FORBIDDEN(HttpStatus.FORBIDDEN, "403", "권한 부족"), //403
//    NOT_FOUND(HttpStatus.NOT_FOUND, "404", "요청 리소스를 찾을 수 없음"), //404
    KAKAO_USER_ALREADY_EXIST(HttpStatus.INTERNAL_SERVER_ERROR, "5001", "해당 카카오 계정으로 회원가입된 사용자 이미 존재함"); //500

    private HttpStatus status;
    private String code;
    private String message;

    UserStatusCode(HttpStatus status, String code, String message){
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
        return DomainLabel.USER.getInitial() + code;
    }

    @Override
    public String getMessage() {
        return DomainLabel.USER.getLabel() + message;
    }
}
