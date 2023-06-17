package hatch.hatchserver2023.global.common.response.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusCodeDoc {
    COMMON("COMMON", "공통", "/status-codes/common", "공통 상태 코드", CommonCode.values()),
    USER("USER", "사용자", "/status-codes/user", "사용자 상태 코드", UserStatusCode.values()),
    ;

    private final String initial;
    private final String label;

    private final String url;
    private final String title;
    private final StatusCode[] statusCodes;

    public String getInitialWithConnector() {
        return this.initial + "-";
    }

    public String getLabelWithBrackets() {
        return "[" + this.label + "] ";
    }
}
