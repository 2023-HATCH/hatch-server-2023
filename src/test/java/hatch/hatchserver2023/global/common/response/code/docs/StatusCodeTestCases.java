package hatch.hatchserver2023.global.common.response.code.docs;

import hatch.hatchserver2023.global.common.response.code.CommonCode;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusCodeTestCases {
    COMMON("/status-codes/common", "common", "공통 상태 코드", CommonCode.values()),
    USER("/status-codes/user", "user", "사용자 상태 코드", UserStatusCode.values())
    ;

    private String url;
    private String sub;
    private String title;
    private StatusCode[] statusCodes;
}
