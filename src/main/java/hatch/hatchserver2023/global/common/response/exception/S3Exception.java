package hatch.hatchserver2023.global.common.response.exception;


import hatch.hatchserver2023.global.common.response.code.StatusCode;

public class S3Exception extends DefaultException {
    public S3Exception(StatusCode code) {
        super(code);
    }
}