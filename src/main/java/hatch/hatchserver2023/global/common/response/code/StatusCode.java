package hatch.hatchserver2023.global.common.response.code;

import org.springframework.http.HttpStatus;

public interface StatusCode {
    String name();
    HttpStatus getStatus();
    String getCode();
    String getMessage();
}
