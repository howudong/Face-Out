package springboot.focusing.handler.kurento;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidate;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.domain.UserSession;
import springboot.focusing.handler.KurentoHandler;
import springboot.focusing.service.UserSessionService;

import java.io.IOException;

@Slf4j
public class ICEHandler implements KurentoHandler {
    @Override
    public void process(WebSocketSession session, UserSessionService userService, JsonObject jsonMessage) throws IOException {
        log.info("ICE Handler Process");
        UserSession userSession = userService.findSession(session);
        IceCandidate candidate = makeIceCandidate(jsonMessage);
        userSession.addCandidate(candidate, jsonMessage.get("name").getAsString());
    }

    @Override
    public void onError() {
        log.error("ICEHandler : Error Occurred");
    }

    private IceCandidate makeIceCandidate(JsonObject jsonMessage) {
        JsonObject jsonCandidate = jsonMessage.get("candidate").getAsJsonObject();
        log.info("MAKE ICE CANDIDATE {}", jsonCandidate);
        return new IceCandidate(
                jsonCandidate.get("candidate").getAsString(),
                jsonCandidate.get("sdpMid").getAsString(),
                jsonCandidate.get("sdpMLineIndex").getAsInt());
    }
}
