package hatch.hatchserver2023.global.config.socket;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.socket.messaging.WebSocketAnnotationMethodMessageHandler;

public class CustomWebSocketAnnotationMethodMessageHandler extends WebSocketAnnotationMethodMessageHandler {
    public CustomWebSocketAnnotationMethodMessageHandler(SubscribableChannel clientInChannel, MessageChannel clientOutChannel, SimpMessageSendingOperations brokerTemplate) {
        super(clientInChannel, clientOutChannel, brokerTemplate);
    }
}
