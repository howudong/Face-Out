package springboot.focusing.controller.kurento;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.controller.KurentoController;
import springboot.focusing.service.UserSessionService;

import java.io.IOException;

@Slf4j
public class ErrorController implements KurentoController {
    @Override
    public void process(WebSocketSession session, UserSessionService userService, JsonObject jsonMessage) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("id", "error");
        response.addProperty("message", "Invalid Message");
        session.sendMessage(new TextMessage(response.toString()));
    }

    @Override
    public void onError() {
        log.error("ErrorHandler : Error Occurred");
    }
}
