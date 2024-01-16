package springboot.focusing.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import springboot.focusing.KurentoHandlerAdapter;
import springboot.focusing.service.UserRegistry;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class MainHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(MainHandler.class);
    private static final Gson gson = new GsonBuilder().create();

    private final KurentoHandlerAdapter kurentoHandlerAdapter;
    private final UserRegistry registry;


    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
        String id = jsonMessage.get("id").getAsString();

        KurentoHandler findHandler = kurentoHandlerAdapter.findHandlerById(id);
        processByHandler(session, jsonMessage, findHandler);
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        registry.findBySessionId(session.getId())
                .ifPresent(userSession ->
                        registry.removeBySession(userSession, session.getId()));
    }

    private void processByHandler(WebSocketSession session, JsonObject jsonMessage, KurentoHandler handler) {
        try {
            handler.process(session, registry, jsonMessage);
        } catch (IOException e) {
            handler.onError();
            log.warn("Error Occurred on {}", handler.getClass());
        }
    }
}
