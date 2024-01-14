package springboot.focusing.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import springboot.focusing.KurentoHandlerAdapter;
import springboot.focusing.service.UserRegistry;

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

        KurentoHandler kurentoHandler = kurentoHandlerAdapter.getHandlerById(id);
        kurentoHandler.process(session, registry, jsonMessage);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        registry.findBySessionId(session.getId())
                .ifPresentOrElse(
                        e -> registry.removeBySession(e, session.getId()),
                        () -> log.error("can not find target WebSocket : {}", session.getId()));
    }
}
