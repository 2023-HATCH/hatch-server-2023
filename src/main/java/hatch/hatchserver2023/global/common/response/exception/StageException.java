package hatch.hatchserver2023.global.common.response.exception;

import hatch.hatchserver2023.global.common.response.code.StatusCode;

public class StageException extends DefaultException {
    public StageException(StatusCode code) {
        super(code);
    }
}
