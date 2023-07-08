package hatch.hatchserver2023.global.common.response.exception;

import hatch.hatchserver2023.global.common.response.code.StatusCode;

public class VideoException extends DefaultException {
    public VideoException(StatusCode code) {
        super(code);
    }
}
