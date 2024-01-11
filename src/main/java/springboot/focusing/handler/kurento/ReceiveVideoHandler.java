package springboot.focusing.handler.kurento;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.domain.UserSession;
import springboot.focusing.handler.KurentoHandler;
import springboot.focusing.service.UserRegistry;

import java.io.IOException;

@RequiredArgsConstructor
public class ReceiveVideoHandler implements KurentoHandler {
    private final UserRegistry registry;

    @Override
    public void process(WebSocketSession session, JsonObject jsonMessage) {
        final String senderName = jsonMessage.get("sender").getAsString();
        final UserSession sender = registry.findByName(senderName);
        final String sdpOffer = jsonMessage.get("sdpOffer").getAsString();

        UserSession receiver = registry.findBySession(session);
        try {
            receiver.receiveVideoFrom(sender, sdpOffer);
        } catch (IOException e) {
        }
        ;
    }

    @Override
    public void onError() {
        //TODO
    }
}
