package hatch.hatchserver2023.global.common.response.code;

import org.springframework.http.HttpStatus;

public enum VideoStatusCode implements StatusCode {

    //2xx
    VIDEO_UPLOAD_SUCCESS(HttpStatus.OK, "2001", "동영상 업로드 성공"), //200
    GET_VIDEO_DETAIL_SUCCESS(HttpStatus.OK, "2002", "동영상 상세 정보 조회 성공"), //200
    GET_VIDEO_LIST_SUCCESS(HttpStatus.OK, "2003", "동영상 목록 정보 조회 성공"), //200
    HASHTAG_SEARCH_SUCCESS(HttpStatus.OK, "2004", "해시태그 검색 성공"),    //200

    //4xx
    VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, "4401", "해당 동영상을 찾을 수 없음"), //404

    //5xx
    CONVERT_MULTIPARTFILE_TO_FILE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "5001", "썸네일 추출 과정 중 multipartfile을 file로 전환 실패"), //500
    CONVERT_FILE_TO_MULTIPARTFILE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "5002", "썸네일 추출 과정 중 file을 multipartfile로 전환 실패"), //500



    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    VideoStatusCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        return this.status;
    }

    @Override
    public String getCode() {
        return StatusCodeDoc.VIDEO.getInitialWithConnector() + this.code;
    }

    @Override
    public String getMessage() {
        return StatusCodeDoc.VIDEO.getInitialWithConnector() + this.message;
    }
}
