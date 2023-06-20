package hatch.hatchserver2023.global.common.response.code.docs;

import hatch.hatchserver2023.global.common.response.code.CommonCode;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
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
    @GetMapping("/common")
    public ResponseEntity<StatusCodeView> getCommonCodes() {
        return getResponse(CommonCode.values());
    }

    @GetMapping("/user")
    public ResponseEntity<StatusCodeView> getUserStatusCodes() {
        return getResponse(UserStatusCode.values());
    }

    private ResponseEntity<StatusCodeView> getResponse(StatusCode[] statusCodes) {
        Map<String, String> statusCodeMap = Arrays.stream(statusCodes)
                .collect(Collectors.toMap(StatusCode::getCode, StatusCode::getMessage));
        return new ResponseEntity<>(new StatusCodeView(statusCodeMap), HttpStatus.OK);
    }

}