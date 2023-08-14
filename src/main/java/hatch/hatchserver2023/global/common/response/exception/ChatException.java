package hatch.hatchserver2023.global.common.response.exception;

import hatch.hatchserver2023.global.common.response.code.StatusCode;

public class ChatException extends DefaultException {
    public ChatException(StatusCode code) {
        super(code);
    }
}
