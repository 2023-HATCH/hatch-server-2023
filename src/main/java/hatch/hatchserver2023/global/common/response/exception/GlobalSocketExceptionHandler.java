package hatch.hatchserver2023.global.common.response.exception;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 소켓 통신 시 발생하는 에러를 핸들링하는 클래스
 */
@Slf4j
@ControllerAdvice
public class GlobalSocketExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String ERROR_WS_SEND_URL = "/topic/errors";

    /**
     * @MessageMapping 된 메서드에서 예외 발생한 경우 (내가 만든 예외 클래스)
     */
    @MessageExceptionHandler(DefaultException.class)
    @SendToUser(ERROR_WS_SEND_URL) // @MessageExceptionHandler 와 함께 사용해야 동작함
    public CommonResponse handleSocketMessageDefaultException(DefaultException e) {
        log.info("[HANDLER] handleSocketMessageDefaultException");
        StatusCode code = e.getCode();
        return CommonResponse.toErrorResponse(code);
    }

    /**
     * @MessageMapping 된 메서드에서 발생한 경우
     * 자식 클래스의 핸들러 우선순위가 더 높으므로, 이 메서드는 가장 마지작 순위로 실행됨
     * @param e
     * @return
     */
    @MessageExceptionHandler(Exception.class)
    @SendToUser(ERROR_WS_SEND_URL)
    public CommonResponse handleSocketMessageException(Exception e) {
        log.info("[HANDLER] handleSocketMessageException");
        return CommonResponse.toErrorResponse(e);
    }
}
