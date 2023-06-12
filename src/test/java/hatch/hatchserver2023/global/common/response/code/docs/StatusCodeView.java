package hatch.hatchserver2023.global.common.response.code.docs;

import hatch.hatchserver2023.global.common.response.code.CommonCode;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class StatusCodeView {
    private Map<String, String> statusCodes; //key : 에러코드, value : 설명


    //모든 상태 코드(일단 CommonCode 만) 모아서 배열로 반환하는 메서드
    public static StatusCode[] getStatusCodeArray() {
        return CommonCode.values();
    }
}
