package hatch.hatchserver2023.global.common.response.code;


import lombok.Getter;
import org.springframework.http.HttpStatus;

//@Getter
public enum S3StatusCode implements StatusCode {

    //2xx
    S3_FILE_UPLOAD_SUCCESS(HttpStatus.OK, "2000", "파일 업로드 성공"), //200

    //4xx


    //5xx
    S3_FILE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "5001", "파일 업로드 실패"), //500,
    FILE_CONVERT_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "5002", "임시 파일로 전환 실패"), //500
    TEMP_FILE_DELETE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "5003", "임시 파일 삭제 실패"), //500
    S3_FILE_DELETE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "5004", "S3 파일 삭제 실패")    //500

    ;

    private final HttpStatus status;
    private final String code;
    private final String message;


    S3StatusCode (HttpStatus status, String code, String message){
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
        return StatusCodeDoc.S3.getInitialWithConnector() + this.code;
    }

    @Override
    public String getMessage() {
        return StatusCodeDoc.S3.getInitialWithConnector() + this.message;
    }
}
