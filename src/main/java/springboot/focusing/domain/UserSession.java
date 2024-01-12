package springboot.focusing.domain;

import com.google.gson.JsonObject;
import org.kurento.client.IceCandidate;
import org.kurento.client.IceCandidateFoundEvent;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class UserSession implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(UserSession.class);
    private final MediaPipeline mediaPipeline;
    private final WebSocketSession session;
    private final String name;
    private final Map<String, WebRtcEndpoint> incomingMedia = new ConcurrentHashMap<>();
    private final WebRtcEndpoint outgoingMedia;

    public UserSession(MediaPipeline mediaPipeline, WebSocketSession session, String name) {
        this.mediaPipeline = mediaPipeline;
        this.session = session;
        this.name = name;
        this.outgoingMedia = new WebRtcEndpoint.Builder(mediaPipeline).build();
        this.outgoingMedia.addIceCandidateFoundListener(this::makeIceJson);
    }

    public void addCandidate(IceCandidate candidate, String name) {
        if (this.name.compareTo(name) == 0) {
            outgoingMedia.addIceCandidate(candidate);
        } else {
            WebRtcEndpoint webRtc = incomingMedia.get(name);
            if (webRtc != null) {
                webRtc.addIceCandidate(candidate);
            }
        }
    }

    public void connectPeer(WebRtcEndpoint incoming) {
        this.outgoingMedia.connect(incoming);
    }

    public void sendMessage(JsonObject message) throws IOException {
        log.info("USER {}: Sending message {}", name, message);
        synchronized (session) {
            session.sendMessage(new TextMessage(message.toString()));
        }
    }

    public void release() {
        this.mediaPipeline.release();
    }

    public String getName() {
        return name;
    }

    @Override
    public void close() throws IOException {
        release();
    }

    private void makeIceJson(IceCandidateFoundEvent event) {
        JsonObject response = new JsonObject();
        response.addProperty("id", "iceCandidate");
        response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
        try {
            sendMessage(response);
        } catch (IOException e) {
        }
        ;
    }

    public void receiveVideoFrom(UserSession sender, String sdpOffer) throws IOException {
        log.info("USER {}: SdpOffer for {} receiveVideoFrom", this.name, sender.getName());

        final String ipSdpAnswer = this.getEndpointForUser(sender).processOffer(sdpOffer);
        final JsonObject scParams = new JsonObject();
        scParams.addProperty("id", "receiveVideoAnswer");
        scParams.addProperty("name", sender.getName());
        scParams.addProperty("sdpAnswer", ipSdpAnswer);

        log.info("USER {}: SdpAnswer for {} receiveVideoAnswer", this.name, sender.getName());
        this.sendMessage(scParams);
        log.info("gather candidates");
        this.getEndpointForUser(sender).gatherCandidates();
    }

    private WebRtcEndpoint getEndpointForUser(final UserSession sender) {
        if (sender.getName().equals(name)) {
            log.info("PARTICIPANT {}: configuring loopback", this.name);
            return outgoingMedia;
        }

        log.info("PARTICIPANT {}: receiving video from {}", this.name, sender.getName());

        WebRtcEndpoint incoming = incomingMedia.get(sender.getName());
        if (incoming == null) {
            log.info("PARTICIPANT {}: creating new endpoint for {}", this.name, sender.getName());
            incoming = new WebRtcEndpoint.Builder(mediaPipeline).build();

            incoming.addIceCandidateFoundListener(event -> {
                JsonObject response = new JsonObject();
                response.addProperty("id", "iceCandidate");
                response.addProperty("name", sender.getName());
                response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
                try {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(response.toString()));
                    }
                } catch (IOException e) {
                    log.debug(e.getMessage());
                }
            });

            incomingMedia.put(sender.getName(), incoming);
        }

        log.info("PARTICIPANT {}: obtained endpoint for {}", this.name, sender.getName());
        sender.connectPeer(incoming);
        return incoming;
    }
}
