package springboot.focusing.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import springboot.focusing.controller.KurentoController;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {
    private static final Gson gson = new GsonBuilder().create();
    private final KurentoHandlerAdapter kurentoHandlerAdapter;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
        String id = jsonMessage.get("id").getAsString();
        log.info("Receive ID [{}] from {} ", id, session.getId());

        KurentoController findController = kurentoHandlerAdapter.findController(id);
        processByController(session, jsonMessage, findController);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        KurentoController controller = kurentoHandlerAdapter.getCloseController();
        processByController(session, null, controller);
    }

    private void processByController(WebSocketSession session, JsonObject jsonMessage, KurentoController controller) {
        try {
            controller.process(session, jsonMessage);
        } catch (IOException e) {
            controller.onError();
            log.warn("Error Occurred on {}", controller.getClass());
        }
    }
}
