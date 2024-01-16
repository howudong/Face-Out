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
        sendVideoSdpAnswer(sender, receiver, sdpOffer);
    }

    public void sendVideoSdpAnswer(UserSession sender, UserSession receiver, String sdpOffer) throws IOException {
        log.info("USER {}: SdpOffer for {} receiveVideoFrom", receiver.getName(), sender.getName());

        final String ipSdpAnswer = receiver.getEndpointForUser(sender).processOffer(sdpOffer);
        final JsonObject scParams = new JsonObject();
        scParams.addProperty("id", "receiveVideoAnswer");
        scParams.addProperty("name", sender.getName());
        scParams.addProperty("sdpAnswer", ipSdpAnswer);

        log.info("USER {}: SdpAnswer for {} receiveVideoAnswer", receiver.getName(), sender.getName());
        receiver.sendMessage(scParams);
        log.info("gather candidates");
        receiver.getEndpointForUser(sender).gatherCandidates();
    }

    @Override
    public void onError() {
        log.error("ReceiveVideoHandler : Error Occurred");
    }

    private UserSession findSenderSession(UserRegistry registry, String senderName) {
        return registry
                .findByName(senderName)
                .orElseThrow(
                        () -> new NoSuchElementException("[ReceiveVideoHandler] Can not find sender"));
    }

    private UserSession findReceiverSession(WebSocketSession session, UserRegistry registry) {
        return registry
                .findBySessionId(session.getId())
                .orElseThrow(()
                        -> new NoSuchElementException("[ReceiveVideoHandler] Can not find receiver"));
    }
}
