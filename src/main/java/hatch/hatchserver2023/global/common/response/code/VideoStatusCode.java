package hatch.hatchserver2023.global.common.response.code;

import org.springframework.http.HttpStatus;

public enum VideoStatusCode implements StatusCode {

    //2xx
    VIDEO_UPLOAD_SUCCESS(HttpStatus.OK, "2001", "동영상 업로드 성공"), //200
    VIDEO_DELETE_SUCCESS(HttpStatus.OK, "2002", "동영상 삭제 성공"),   //200
    GET_VIDEO_DETAIL_SUCCESS(HttpStatus.OK, "2003", "동영상 상세 정보 조회 성공"), //200
    GET_VIDEO_LIST_SUCCESS_FOR_USER(HttpStatus.OK, "2004", "회원 동영상 목록 정보 조회 성공"), //200
    GET_VIDEO_LIST_SUCCESS_FOR_ANONYMOUS(HttpStatus.OK, "2005", "비회원 동영상 목록 정보 조회 성공"), //200
    HASHTAG_SEARCH_SUCCESS_FOR_USER(HttpStatus.OK, "2010", "회원 해시태그 검색 성공"),    //200
    HASHTAG_SEARCH_SUCCESS_FOR_ANONYMOUS(HttpStatus.OK, "2011", "비회원 해시태그 검색 성공"),    //200
    GET_HASHTAG_LIST_SUCCESS(HttpStatus.OK, "2012", "해시태그 목록 조회 성공"),   //200
    COMMENT_REGISTER_SUCCESS(HttpStatus.OK, "2020", "댓글 등록 성공"),    //200
    COMMENT_DELETE_SUCCESS(HttpStatus.OK, "2021", "댓글 삭제 성공"),      //200
    GET_COMMENT_LIST_SUCCESS(HttpStatus.OK, "2022", "댓글 목록 조회 성공"), //200
    LIKE_ADD_SUCCESS(HttpStatus.OK, "2030", "좋아요 등록 성공"),   //200
//    ALREADY_LIKED_BUT_SUCCESS(HttpStatus.OK, "2031", "이미 좋아요를 눌러서 새로운 좋아요를 추가하지 않았습니다."),   //200
    LIKE_DELETE_SUCCESS(HttpStatus.OK, "2031", "좋아요 삭제 성공"),    //200
    GET_LIKE_VIDEO_LIST_SUCCESS_FOR_USER(HttpStatus.OK, "2032", "회원 좋아요 누른 영상 목록 조회 성공"),    //200
    GET_LIKE_VIDEO_LIST_SUCCESS_FOR_ANONYMOUS(HttpStatus.OK, "2033", "비회원 좋아요 누른 영상 목록 조회 성공"),    //200


    //4xx
    VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, "4401", "해당 동영상을 찾을 수 없음"), //404
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "4420", "해당 댓글을 찾을 수 없음"),  //404
    NOT_YOUR_COMMENT(HttpStatus.UNAUTHORIZED, "4121", "댓글 작성자가 아닌 사용자가 댓글을 삭제할 수 없습니다."),   //401
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "4430", "해당 좋아요를 찾을 수 없습니다"),     //404
    ALREADY_LIKED(HttpStatus.BAD_REQUEST, "4031", "이미 좋아요를 눌러서 새로운 좋아요를 추가할 수 없습니다"), //400


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
