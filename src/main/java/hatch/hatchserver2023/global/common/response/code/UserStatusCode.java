package hatch.hatchserver2023.global.common.response.code;

import org.springframework.http.HttpStatus;

//@Getter
public enum UserStatusCode implements StatusCode {

    // 2xx
    KAKAO_LOGIN_SUCCESS(HttpStatus.OK, "2000", "카카오 회원가입 및 로그인 성공"), //200
    GET_PROFILE_SUCCESS(HttpStatus.OK, "2011", "사용자 프로필 조회 성공"),    //200
    GET_USERS_VIDEO_LIST_SUCCESS_FOR_USER(HttpStatus.OK, "2012", "회원: 사용자가 업로드한 영상 목록 조회 성공"),  //200
    GET_USERS_VIDEO_LIST_SUCCESS_FOR_ANONYMOUS(HttpStatus.OK, "2013", "비회원: 사용자가 업로드한 영상 목록 조회 성공"),  //200
    UPDATE_MY_PROFILE_SUCCESS(HttpStatus.OK, "2014", "프로필 수정 성공"),  //200
    SEARCH_USERS_SUCCESS(HttpStatus.OK, "2015", "계정 검색 성공"),    //200
    ADD_FOLLOW_SUCCESS(HttpStatus.OK, "2021", "팔로우 등록 성공"), //200
    DELETE_FOLLOW_SUCCESS(HttpStatus.OK, "2022", "팔로우 삭제 성공"), //200
    GET_FOLLOW_LIST_SUCCESS_FOR_USER(HttpStatus.OK, "2023", "회원: 팔로잉/팔로워 목록 조회 성공"), //200
    GET_FOLLOW_LIST_SUCCESS_FOR_ANONYMOUS(HttpStatus.OK, "2024", "비회원: 팔로잉/팔로워 목록 조회 성공"), //200
    GET_ALL_USER_SUCCESS(HttpStatus.OK, "2031", "로그인 유저를 제외한 전체 사용자 목록 조회 성공"), //200

    KAKAO_LOGOUT_SUCCESS(HttpStatus.NO_CONTENT, "2400", "카카오 로그인 사용자의 로그아웃 성공"), //204


    // 4xx
    LOGIN_ID_NOT_EXIST(HttpStatus.UNAUTHORIZED, "4101", "로그인 아이디 존재하지 않음"), //401

    TOKEN_CANNOT_RESOLVE(HttpStatus.UNAUTHORIZED, "4102", "토큰에서 사용자 정보를 가져올 수 없음"), //401
    UUID_NOT_FOUND(HttpStatus.UNAUTHORIZED, "4103", "이 UUID 에 해당하는 사용자 없음"), //401
    UUID_IS_NULL(HttpStatus.UNAUTHORIZED, "4104", "UUID 값이 비었음"), //401
    COOKIE_LIST_IS_EMPTY(HttpStatus.UNAUTHORIZED, "4105", "쿠키 목록이 비었음"), //401
    FCM_NOTIFICATION_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "4106", "알림을 보낼 수 없음. FCM 알림 토큰이 존재하지 않는 사용자임"), //401

    USER_PRE_FORBIDDEN(HttpStatus.FORBIDDEN, "4306", "사용자 권한 부족 (api 메서드 단위)"), //403

//    NOT_FOUND(HttpStatus.NOT_FOUND, "404", "요청 리소스를 찾을 수 없음"), //404
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "4401", "PK에 해당하는 사용자를 찾을 수 없음"), //404
    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "4402", "해당 Follow를 찾을 수 없음"), //404


    // 5xx
    LOGIN_TYPE_NOT_KAKAO(HttpStatus.INTERNAL_SERVER_ERROR, "5001", "type 값이 kakao가 아님. 카카오로그인 방식만 사용 가능함"), //500
    KAKAO_USER_ALREADY_EXIST(HttpStatus.INTERNAL_SERVER_ERROR, "5002", "해당 카카오 계정으로 회원가입된 사용자 이미 존재함"), //500
    KAKAO_CONNECTION_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "5003", "카카오 사용자 정보 가져오기 실패"), //500
    KAKAO_NICKNAME_EMPTY(HttpStatus.INTERNAL_SERVER_ERROR, "5004", "카카오 사용자 정보에 닉네임 값이 없음"), //500
    CANT_FOLLOW_YOURSELF(HttpStatus.INTERNAL_SERVER_ERROR, "5011", "자기 자신을 팔로우할 수 없음"),     //500
    ALREADY_FOLLOWED(HttpStatus.INTERNAL_SERVER_ERROR, "5012", "이미 팔로우 했음")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

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
        return StatusCodeDoc.USER.getInitialWithConnector() + code;
    }

    @Override
    public String getMessage() {
        return StatusCodeDoc.USER.getLabelWithBrackets() + message;
    }
}
