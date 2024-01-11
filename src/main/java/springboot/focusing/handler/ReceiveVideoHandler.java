package springboot.focusing.handler;

import com.google.gson.JsonObject;
import org.springframework.web.socket.WebSocketSession;

public class ReceiveVideoHandler implements KurentoHandler {
    @Override
    public void process(WebSocketSession session, JsonObject jsonMessage) {
        String name = jsonMessage.get("sender").getAsString();
        String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
        System.out.println("name = " + name);
        System.out.println("sdpOffer = " + sdpOffer);
    }

    @Override
    public void onError() {

    }
}
