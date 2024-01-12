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

import java.io.IOException;
import java.util.Optional;

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

        Optional<KurentoHandler> kurentoHandler = kurentoHandlerAdapter.getHandlerById(id);
        if (kurentoHandler.isEmpty()) {
            sendError(session);
            return;
        }
        try {
            kurentoHandler.get().process(session, registry, jsonMessage);
        } catch (IOException e) {
            kurentoHandler.get().onError();
        }
    }

    private void sendError(WebSocketSession session) {
        try {
            JsonObject response = new JsonObject();
            response.addProperty("id", "error");
            response.addProperty("message", "Invalid Message");
            session.sendMessage(new TextMessage(response.toString()));
        } catch (IOException e) {
            log.error("Exception sending message", e);
        }
    }
}
