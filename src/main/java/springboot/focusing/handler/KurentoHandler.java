package springboot.focusing.handler;

import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public interface KurentoHandler {
    void process(WebSocketSession session, JsonObject jsonMessage);

    void onError();
}
