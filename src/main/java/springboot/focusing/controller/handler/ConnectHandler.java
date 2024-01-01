package springboot.focusing.controller.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.kurento.client.IceCandidate;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import springboot.focusing.domain.UserSession;
import springboot.focusing.service.UserRegistry;

import java.io.IOException;

@RequiredArgsConstructor
public class ConnectHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(ConnectHandler.class);
    private static final Gson gson = new GsonBuilder().create();

    private final UserRegistry registry;
    @Autowired
    private KurentoClient kurentoClient;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
        UserSession user = registry.findBySessionId(session.getId());
        log.debug("Incoming message from new user: {}", jsonMessage);
        switch (jsonMessage.get("id").getAsString()) {
            case "join":
                join(session, jsonMessage);
                break;

            case "onIceCandidate": {
                JsonObject jsonCandidate = jsonMessage.get("candidate").getAsJsonObject();
                if (user != null) {
                    IceCandidate candidate = new IceCandidate(jsonCandidate.get("candidate").getAsString(),
                            jsonCandidate.get("sdpMid").getAsString(),
                            jsonCandidate.get("sdpMLineIndex").getAsInt());
                    user.addCandidate(candidate, session.getId());
                }
                break;
            }
            default:
                sendError(session, "Invalid Message");
        }
    }

    private void sendError(WebSocketSession session, String message) {
        try {
            JsonObject response = new JsonObject();
            response.addProperty("id", "error");
            response.addProperty("message", message);
            session.sendMessage(new TextMessage(response.toString()));
        } catch (IOException e) {
            log.error("Exception sending message", e);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        registry.removeBySession(session);
    }

    private void join(WebSocketSession session, JsonObject jsonMessage) {
        UserSession user = createUserSession(session);
        registry.register(session.getId(), user);
        log.info("PARTICIPANT: trying to join room");
    }

    private UserSession createUserSession(WebSocketSession session) {
        MediaPipeline pipeline = kurentoClient.createMediaPipeline();
        return new UserSession(pipeline, session);
    }
}
