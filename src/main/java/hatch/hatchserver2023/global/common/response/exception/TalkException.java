package hatch.hatchserver2023.global.common.response.exception;

import hatch.hatchserver2023.global.common.response.code.StatusCode;

public class TalkException extends DefaultException {
    public TalkException(StatusCode code) {
        super(code);
    }
}
