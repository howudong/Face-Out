package springboot.focusing.handler.kurento;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidate;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.domain.UserSession;
import springboot.focusing.handler.KurentoHandler;
import springboot.focusing.service.UserRegistry;

@RequiredArgsConstructor
@Slf4j
public class ICEHandler implements KurentoHandler {
    private final UserRegistry registry;

    @Override
    public void process(WebSocketSession session, JsonObject jsonMessage) {
        JsonObject jsonCandidate = jsonMessage.get("candidate").getAsJsonObject();
        String name = jsonMessage.get("name").getAsString();
        UserSession user = registry.findBySession(session);
        if (user != null) {
            IceCandidate candidate = new IceCandidate(jsonCandidate.get("candidate").getAsString(),
                    jsonCandidate.get("sdpMid").getAsString(),
                    jsonCandidate.get("sdpMLineIndex").getAsInt());
            user.addCandidate(candidate, name);
        }
    }

    @Override
    public void onError() {
        //TODO
    }
}
