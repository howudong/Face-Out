package springboot.focusing.controller;

import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.service.UserSessionService;

import java.io.IOException;

@Component
public interface KurentoController {
    void process(WebSocketSession session, UserSessionService userService, JsonObject jsonMessage) throws IOException;

    void onError();
}
