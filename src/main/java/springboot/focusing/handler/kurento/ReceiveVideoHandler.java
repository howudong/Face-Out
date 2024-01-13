package springboot.focusing.handler.kurento;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.domain.UserSession;
import springboot.focusing.handler.KurentoHandler;
import springboot.focusing.service.UserRegistry;

import java.io.IOException;

@Slf4j
public class ReceiveVideoHandler implements KurentoHandler {

    @Override
    public void process(WebSocketSession session, UserRegistry registry, JsonObject jsonMessage) throws IOException {
        final String senderName = jsonMessage.get("sender").getAsString();
        final UserSession sender = registry.findByName(senderName);
        final String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
        log.info("ReceiveVideoHandler : {}", senderName, sdpOffer);

        UserSession receiver = registry.findBySession(session);
        receiver.receiveVideoFrom(sender, sdpOffer);
    }

    @Override
    public void onError() {
        //TODO
    }
}
