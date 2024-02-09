package springboot.focusing.controller.kurento;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.controller.KurentoController;
import springboot.focusing.domain.UserSession;
import springboot.focusing.service.UserSessionService;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ICEController implements KurentoController {
    private final UserSessionService userService;

    @Override
    public void process(WebSocketSession session, JsonObject jsonMessage) throws IOException {
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
