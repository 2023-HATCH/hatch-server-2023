package hatch.hatchserver2023.global.common.response.exception;

import hatch.hatchserver2023.global.common.response.code.StatusCode;

public class UserException extends DefaultException{
    public UserException(StatusCode code) {
        super(code);
    }
}
