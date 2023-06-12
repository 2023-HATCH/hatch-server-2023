package hatch.hatchserver2023.global.common.response.code.docs;

import hatch.hatchserver2023.global.common.response.code.StatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/status-codes")
public class StatusCodeController {
    @GetMapping
    public ResponseEntity<StatusCodeView> getStatusCodes() {
//        Map<String, String> statusCodes = Arrays.stream(StatusCode.values()) //모든 StatusCode 들을 모아서 리스트로 주는 메서드..?
        Map<String, String> statusCodes = Arrays.stream(StatusCodeView.getStatusCodeArray())
                .collect(Collectors.toMap(StatusCode::getCode, StatusCode::getMessage));

        return new ResponseEntity<>(new StatusCodeView(statusCodes), HttpStatus.OK);
    }


    //StatusCode.values() : 원글은 StatusCode 가 interface가 아니라 enum class 였고 enum기본제공 메서드.
    // 난 이걸 모든 코드(또는 일단 Common만) 모아서 주는 메서드 하나를 어디서 만들어야 할 듯
}
