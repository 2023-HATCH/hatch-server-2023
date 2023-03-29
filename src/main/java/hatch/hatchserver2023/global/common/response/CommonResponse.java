package hatch.hatchserver2023.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import lombok.Builder;
import lombok.Data;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse {
    private final String timeStamp = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
            .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
    private final String code;
    private final String message;
    private final Object data;

    public static CommonResponse toResponse(StatusCode statusCode, Object data) {
        return CommonResponse.builder()
                .code(statusCode.getCode())
                .message(statusCode.getMessage())
                .data(data)
                .build();
    }

    public static CommonResponse toResponse(StatusCode statusCode) {
        return CommonResponse.builder()
                .code(statusCode.getCode())
                .message(statusCode.getMessage())
                .build();
    }

    public static CommonResponse toErrorResponse(StatusCode statusCode, String message) {
        return CommonResponse.builder()
                .code(statusCode.getCode())
                .message(message)
                .build();
    }


    public static CommonResponse toErrorResponse(StatusCode statusCode) {
        return CommonResponse.builder()
                .code(statusCode.getCode())
                .message(statusCode.getMessage())
                .build();
    }
}
