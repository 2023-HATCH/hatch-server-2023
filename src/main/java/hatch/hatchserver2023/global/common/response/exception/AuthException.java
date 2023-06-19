package hatch.hatchserver2023.global.common.response.exception;

import hatch.hatchserver2023.global.common.response.code.StatusCode;

public class AuthException extends DefaultException {
    public AuthException(StatusCode code) {
        super(code);
    }
}
