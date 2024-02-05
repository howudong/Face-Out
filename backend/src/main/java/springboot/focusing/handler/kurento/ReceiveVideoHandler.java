package springboot.focusing.handler.kurento;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.Continuation;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.domain.UserSession;
import springboot.focusing.handler.KurentoHandler;
import springboot.focusing.service.UserSessionService;

import java.io.IOException;

@Slf4j
public class ReceiveVideoHandler implements KurentoHandler {

    @Override
    public void process(WebSocketSession session, UserSessionService userService, JsonObject jsonMessage) throws IOException {
        final String senderName = jsonMessage.get("sender").getAsString();
        final UserSession sender = findSenderSession(userService, senderName);
        final String sdpOffer = jsonMessage.get("sdpOffer").getAsString();

        UserSession receiver = findReceiverSession(session, userService);
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
        receiver.getEndpointForUser(sender).gatherCandidates(new Continuation<Void>() {
            @Override
            public void onSuccess(Void result) throws Exception {
                log.info("USER {} : gatherCandidates Success for {}", receiver.getName(), sender.getName());
            }

            @Override
            public void onError(Throwable cause) throws Exception {
                log.warn("USER {} : gatherCandidates fail for {}", receiver.getName(), sender.getName());
                cause.printStackTrace();
            }
        });
    }

    @Override
    public void onError() {
        log.error("ReceiveVideoHandler : Error Occurred");
    }

    private UserSession findSenderSession(UserSessionService registry, String senderName) {
        return registry.findSession(senderName);
    }

    private UserSession findReceiverSession(WebSocketSession session, UserSessionService registry) {
        log.debug("try to get ReceiverSession");
        return registry.findSession(session);
    }
}
