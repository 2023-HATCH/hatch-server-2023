package hatch.hatchserver2023.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import hatch.hatchserver2023.global.common.response.code.CommonCode;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import hatch.hatchserver2023.global.common.response.socket.SocketResponseType;
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
    private final String type;
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

    public static CommonResponse toErrorResponse(Exception e) {
        return CommonResponse.builder()
                .code(CommonCode.INTERNAL_SERVER_ERROR.getCode())
                .message(e.getMessage())
                .build();
    }

    public static CommonResponse toSocketResponse(SocketResponseType type, Object data) {
        return CommonResponse.builder()
                .type(type.getType())
                .message(type.getMessage())
                .data(data)
                .build();
    }

    public static CommonResponse toSocketResponse(SocketResponseType type) {
        return CommonResponse.builder()
                .type(type.getType())
                .message(type.getMessage())
                .build();
    }
}
