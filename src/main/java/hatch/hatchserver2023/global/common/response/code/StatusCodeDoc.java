package hatch.hatchserver2023.global.common.response.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusCodeDoc {
    COMMON("COMMON", "공통", "/common", "공통 상태 코드", CommonCode.values()),
    USER("USER", "사용자", "/user", "사용자 상태 코드", UserStatusCode.values()),
    STAGE("STAGE", "스테이지", "/stage", "스테이지 상태 코드", StageStatusCode.values()),
    TALK("TALK", "라이브톡", "/talk", "라이브톡 상태 코드", TalkStatusCode.values()),
    S3("S3", "S3", "/s3", "S3 상태 코드", S3StatusCode.values()),
    VIDEO("VIDEO", "동영상", "/video", "동영상 상태 코드", VideoStatusCode.values())
    ;

    private final String initial;
    private final String label;

    private final String url;
    private final String subtitle;
    private final StatusCode[] statusCodes;

    public String getFullUrl() {
        return "/status-codes" + this.url;
    }

    public String getInitialWithConnector() {
        return this.initial + "-";
    }

    public String getLabelWithBrackets() {
        return "[" + this.label + "] ";
    }
}
