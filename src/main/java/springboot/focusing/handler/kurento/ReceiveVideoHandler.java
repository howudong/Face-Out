package springboot.focusing.handler.kurento;

import com.google.gson.JsonObject;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.domain.UserSession;
import springboot.focusing.handler.KurentoHandler;
import springboot.focusing.service.UserRegistry;

import java.io.IOException;

public class ReceiveVideoHandler implements KurentoHandler {

    @Override
    public void process(WebSocketSession session, UserRegistry registry, JsonObject jsonMessage) throws IOException {
        final String senderName = jsonMessage.get("sender").getAsString();
        final UserSession sender = registry.findByName(senderName);
        final String sdpOffer = jsonMessage.get("sdpOffer").getAsString();

        UserSession receiver = registry.findBySession(session);
        receiver.receiveVideoFrom(sender, sdpOffer);
    }

    @Override
    public void onError() {
        //TODO
    }
}
