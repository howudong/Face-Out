package springboot.focusing.handler.kurento;

import com.google.gson.JsonObject;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.handler.KurentoHandler;

public class ReceiveVideoHandler implements KurentoHandler {
    @Override
    public void process(WebSocketSession session, JsonObject jsonMessage) {
        String name = jsonMessage.get("sender").getAsString();
        String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
    }

    @Override
    public void onError() {
        //TODO
    }
}
