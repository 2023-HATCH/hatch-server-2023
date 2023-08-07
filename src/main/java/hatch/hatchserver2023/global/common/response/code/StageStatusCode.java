package hatch.hatchserver2023.global.common.response.code;

import org.springframework.http.HttpStatus;

public enum StageStatusCode implements StatusCode {
    // 2xx
    GET_STAGE_ENTER_SUCCESS(HttpStatus.OK, "2001", "스테이지 입장 성공"), //200
    GET_STAGE_EXIT_SUCCESS(HttpStatus.OK, "2002", "스테이지 퇴장 성공"), //200 // TODO : 삭제 예정
    GET_STAGE_ENTER_USER_LIST_SUCCESS(HttpStatus.OK, "2003", "스테이지 내 사용자 목록 조회 성공"), //200
    GET_CATCH_SUCCESS(HttpStatus.OK, "2004", "캐치 요청 성공"), //200

    // 4xx
    MUSIC_NOT_FOUND(HttpStatus.NOT_FOUND, "4401", "음악 제목에 해당하는 스테이지 음악을 찾을 수 없음"), //404

    // 5xx
    ALREADY_ENTERED_USER(HttpStatus.INTERNAL_SERVER_ERROR, "5001", "스테이지에 이미 입장되어있는 사용자임"), //500

    STAGE_ALREADY_EMPTY(HttpStatus.INTERNAL_SERVER_ERROR, "5002", "입장한 인원수가 0임. 퇴장 불가"), //500
    STAGE_STATUS_NOT_CATCH(HttpStatus.INTERNAL_SERVER_ERROR, "5003", "스테이지 상태가 캐치가 아님"), //500
    STAGE_STATUS_NOT_PLAY(HttpStatus.INTERNAL_SERVER_ERROR, "5004", "스테이지 상태가 플레이가 아님"), //500
//    STAGE_PLAYER_NUM_OUT_OF_BOUNDS(HttpStatus.INTERNAL_SERVER_ERROR, "5005", "스테이지 플레이어 번호가 유효 범위를 벗어남"), //500
    FAIL_SAVE_MVP_USER_INFO_JSON(HttpStatus.INTERNAL_SERVER_ERROR, "5006", "스테이지 플레이어 사용자 정보 json 변환 저장 실패"), //500
    FAIL_GET_MVP_USER_INFO_FROM_REDIS_JSON(HttpStatus.INTERNAL_SERVER_ERROR, "5007", "스테이지 mvp 사용자 정보 json 맵핑 실패"), //500
    STAGE_STATUS_NOT_MVP(HttpStatus.INTERNAL_SERVER_ERROR, "5008", "스테이지 상태가 캐치가 아님") //500
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
