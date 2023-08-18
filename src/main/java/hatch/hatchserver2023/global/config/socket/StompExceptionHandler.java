package hatch.hatchserver2023.global.config.socket;

import hatch.hatchserver2023.global.common.response.code.CommonCode;
import hatch.hatchserver2023.global.common.response.exception.ChatException;
import hatch.hatchserver2023.global.common.response.exception.DefaultException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@Component
public class StompExceptionHandler extends StompSubProtocolErrorHandler {
    /**
     * => 여기서는 interceptor 단에서 발생한 에러만 잡아주는 듯..
     *
     * 원래 Stomp 에러는
     *     StompSubProtocolErrorHandler 의 handleClientMessageProcessingError() 를 호출해 처리하고
     *     처리가 끝나면 handerInternal() 을 호출해 메세지를 전송함.
     *
     *     내가 이 메서드를 오버라이딩해서 내가 정의한 예외이면 인터셉트해서 처리하는 것!
     */

    private static final byte[] EMPTY_PAYLOAD = new byte[0];

//    public StompExceptionHandler() {
//        super();
//    }


    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        log.info("[StompExceptionHandler] handleClientMessageProcessingError");

        final Throwable exception = convertThrowException(ex);

        log.info("[StompExceptionHandler] exception : {}", exception.getMessage());
        if(exception instanceof DefaultException) { //TODO : ? 내가 핸들링 원하는 Exception 만 설정해야 할 듯
            log.info("[StompExceptionHandler] instanceof DefaultException : true");

            return handleDefaultException(clientMessage, ex);
        }

        return super.handleClientMessageProcessingError(clientMessage, ex);
    }

    // TODO : ?
    private Throwable convertThrowException(Throwable exception) {
        if(exception instanceof MessageDeliveryException) { // TODO : 뭐지? 이게 언제 나는 에러지?
            return exception.getCause();
        }
        return exception;
    }


    /**
     * DefaultException 이 발생했을 때 핸들링할 메서드
     * @param clientMessage
     * @param ex
     * @return
     */
    private Message<byte[]> handleDefaultException(Message<byte[]> clientMessage, Throwable ex) {
        return makeErrorMessage(clientMessage, ex.getMessage(), CommonCode.INTERNAL_SERVER_ERROR.getMessage());
    }

    /**
     * 소켓에 보낼 에러 응답을 만드는 메서드
     * @param clientMessage
     * @param message
     * @param errorCode
     * @return
     */
    private Message<byte[]> makeErrorMessage(Message<byte[]> clientMessage, String message, String errorCode) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setMessage(errorCode);
        accessor.setLeaveMutable(true); //TODO : ?

        setReceiptForClient(clientMessage, accessor);
        log.info("[StompExceptionHandler] makeErrorMessage message : {}", message);

        return MessageBuilder.createMessage(
                message != null ? message.getBytes(StandardCharsets.UTF_8) : EMPTY_PAYLOAD, // message가 null이면 빈 값 반환
                accessor.getMessageHeaders()
        );
    }

    /**
     * clientMessage의 StompHeaderAccessor 에 receiptId 설정을 하는 메서드. receipt가 뭐지?
     * @param clientMessage
     * @param accessor
     */
    private void setReceiptForClient(Message<byte[]> clientMessage, StompHeaderAccessor accessor) {
        if(Objects.isNull(clientMessage)) {
            return;
        }

        StompHeaderAccessor clientHeaderAccessor = MessageHeaderAccessor.getAccessor(clientMessage, StompHeaderAccessor.class);

        String receiptId = Objects.isNull(clientHeaderAccessor) ? null : clientHeaderAccessor.getReceipt(); //TODO : receipt가 뭐지?

        if(receiptId != null) {
            accessor.setReceiptId(receiptId);
        }

    }


    @Override
    protected Message<byte[]> handleInternal(StompHeaderAccessor errorHeaderAccessor, byte[] errorPayload, Throwable cause, StompHeaderAccessor clientHeaderAccessor) {
        log.info("[StompExceptionHandler] handleInternal");
        return MessageBuilder.createMessage(errorPayload, errorHeaderAccessor.getMessageHeaders());

//        return super.handleInternal(errorHeaderAccessor, errorPayload, cause, clientHeaderAccessor);
    }
}
