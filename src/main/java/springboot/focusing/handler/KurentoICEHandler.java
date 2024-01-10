package springboot.focusing.handler;

import com.google.gson.JsonObject;
import org.springframework.web.socket.WebSocketSession;

public class KurentoICEHandler implements KurentoHandler {
    @Override
    public void process(WebSocketSession session, JsonObject jsonMessage) {

    }

    @Override
    public void onError() {

    }
}
