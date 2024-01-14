package springboot.focusing.handler.kurento;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidate;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.handler.KurentoHandler;
import springboot.focusing.service.UserRegistry;

import java.io.IOException;

@Slf4j
public class ICEHandler implements KurentoHandler {
    @Override
    public void process(WebSocketSession session, UserRegistry registry, JsonObject jsonMessage) throws IOException {
        registry.findBySessionId(session.getId())
                .ifPresent(user -> {
                    IceCandidate candidate = makeIceCandidate(jsonMessage);
                    user.addCandidate(candidate, jsonMessage.get("name").getAsString());
                });
    }

    @Override
    public void onError() {
        //TODO
    }

    private IceCandidate makeIceCandidate(JsonObject jsonMessage) {
        JsonObject jsonCandidate = jsonMessage.get("candidate").getAsJsonObject();

        return new IceCandidate(
                jsonCandidate.get("candidate").getAsString(),
                jsonCandidate.get("sdpMid").getAsString(),
                jsonCandidate.get("sdpMLineIndex").getAsInt());
    }
}
