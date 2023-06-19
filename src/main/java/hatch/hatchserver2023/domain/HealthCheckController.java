package hatch.hatchserver2023.domain;

import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.CommonCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HealthCheckController {
    @GetMapping
    public ResponseEntity<CommonResponse> healthCheck() {
        log.info("[API] GET /");
        return ResponseEntity.ok()
                .body(CommonResponse.toResponse(CommonCode.OK, "Pocket Pose API Server : Health check ok. Server is running"));
    }
}
