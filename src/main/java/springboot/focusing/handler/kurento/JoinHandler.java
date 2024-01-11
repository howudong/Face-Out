package springboot.focusing.handler.kurento;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import springboot.focusing.domain.UserSession;
import springboot.focusing.handler.KurentoHandler;
import springboot.focusing.service.UserRegistry;

@RequiredArgsConstructor
@Slf4j
public class JoinHandler extends TextWebSocketHandler implements KurentoHandler {
    private final UserRegistry registry;
    private final KurentoClient kurentoClient;

    @Override
    public void process(WebSocketSession session, JsonObject jsonMessage) {
        UserSession user = createUserSession(session, jsonMessage);
        registry.register(session.getId(), user);
        log.info("PARTICIPANT: trying to join room");
    }

    @Override
    public void onError() {
        //TODO
    }

    private UserSession createUserSession(WebSocketSession session, JsonObject jsonMessage) {
        String name = jsonMessage.get("name").getAsString();
        MediaPipeline pipeline = kurentoClient.createMediaPipeline();
        return new UserSession(pipeline, session, name);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        registry.removeBySession(session);
    }
}
