package springboot.focusing.handler.kurento;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.domain.UserSession;
import springboot.focusing.handler.KurentoHandler;
import springboot.focusing.service.UserRegistry;

import java.io.IOException;
import java.util.NoSuchElementException;

@Slf4j
public class ReceiveVideoHandler implements KurentoHandler {

    @Override
    public void process(WebSocketSession session, UserRegistry registry, JsonObject jsonMessage) throws IOException {
        final String senderName = jsonMessage.get("sender").getAsString();
        final UserSession sender = findSenderSession(registry, senderName);
        final String sdpOffer = jsonMessage.get("sdpOffer").getAsString();

        UserSession receiver = findReceiverSession(session, registry);
        receiver.receiveVideoFrom(sender, sdpOffer);
    }

    @Override
    public void onError() {
        log.error("ReceiveVideoHandler : Error Occured");
    }

    private UserSession findSenderSession(UserRegistry registry, String senderName) {
        return registry.findByName(senderName)
                .orElseThrow(
                        () -> new NoSuchElementException("[ReceiveVideoHandler] Can not find sender"));
    }

    private UserSession findReceiverSession(WebSocketSession session, UserRegistry registry) {
        return registry.findBySessionId(session.getId())
                .orElseThrow(()
                        -> new NoSuchElementException("[ReceiveVideoHandler] Can not find receiver"));
    }
}
