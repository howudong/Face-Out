package springboot.focusing.handler;

import com.google.gson.JsonObject;
import org.springframework.web.socket.WebSocketSession;

public interface KurentoHandler {
    void process(WebSocketSession session, JsonObject jsonMessage);

    void onError();
}
