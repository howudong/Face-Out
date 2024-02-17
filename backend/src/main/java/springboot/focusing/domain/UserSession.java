package springboot.focusing.domain;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.*;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.dto.ICEDto;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class UserSession implements Closeable {
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
        this.outgoingMedia.addIceCandidateFoundListener(event -> makeIceJson(event, name));
    }

    public void addCandidate(IceCandidate candidate, String name) {
        if (this.name.compareTo(name) == 0) {
            log.info("USER {} : outgoingMedia.addIceCandidate : {} ", this.name, name);
            outgoingMedia.addIceCandidate(candidate);
        } else {
            WebRtcEndpoint webRtc = incomingMedia.get(name);
            if (webRtc != null) {
                log.info("USER {} : incoming.addIceCandidate to {} ", this.name, name);
                webRtc.addIceCandidate(candidate);
            }
        }
    }

    public void connectPeer(UserSession sender, WebRtcEndpoint incoming) {
        sender.outgoingMedia.connect(incoming, new Continuation<Void>() {
            @Override
            public void onSuccess(Void result) throws Exception {
                log.info("connectPeer Success with name : {}", incoming.getName());
            }

            @Override
            public void onError(Throwable cause) throws Exception {
                log.warn("connectPeer Fail with name : {}", incoming.getName());
                cause.printStackTrace();
            }
        });
    }

    public void sendMessage(JsonObject message) throws IOException {
        log.info("USER {}: Sending message {}", name, message);
        synchronized (session) {
            session.sendMessage(new TextMessage(message.toString()));
        }
    }

    public void addNewIncomingMedia(UserSession sender, WebRtcEndpoint newIncoming) {
        if (incomingMedia.containsKey(sender.name)) {
            log.warn("[ADD FAIL] PARTICIPANT {} already exist", sender.name);
            return;
        }
        log.info("PARTICIPANT {}: obtained endpoint for {}", this.name, sender.name);
        connectPeer(sender, newIncoming);
    }

    public WebRtcEndpoint getEndpointForUser(final UserSession sender) {
        if (sender.name.equals(name)) {
            log.info("PARTICIPANT {}: configuring loopback", this.name);
            return outgoingMedia;
        }

        log.info("PARTICIPANT {}: receiving video from {}", this.name, sender.name);

        WebRtcEndpoint incoming = incomingMedia.get(sender.name);
        if (incoming == null) {
            log.info("PARTICIPANT {}: creating new endpoint for {}", this.name, sender.name);
            incoming = new WebRtcEndpoint.Builder(mediaPipeline).useDataChannels().build();
            incoming.addIceCandidateFoundListener(event -> makeIceJson(event, sender.name));
            incomingMedia.put(sender.name, incoming);
        }
        
        addNewIncomingMedia(sender, incoming);
        return incoming;
    }

    public void cancelVideoFrom(final String senderName) {
        log.debug("PARTICIPANT {}: canceling video reception from {}", this.name, senderName);
        log.debug("PARTICIPANT {}: removing endpoint for {}", this.name, senderName);

        final WebRtcEndpoint incoming = incomingMedia.remove(senderName);
        if (incomingMedia.isEmpty()) {
            log.warn("incoming related to {} is not found", senderName);
            return;
        }
        checkIfSuccess(senderName, incoming);
    }

    public boolean isSameSessionId(String sessionId) {
        return this.session
                .getId()
                .equals(sessionId);
    }


    public String getName() {
        return name;
    }

    @Override
    public void close() throws IOException {
        log.debug("PARTICIPANT {}: Releasing resources", this.name);
        for (final String remoteParticipantName : incomingMedia.keySet()) {
            log.trace("PARTICIPANT {}: Released incoming EP for {}", this.name, remoteParticipantName);
            final WebRtcEndpoint ep = this.incomingMedia.get(remoteParticipantName);

            checkIfSuccess(remoteParticipantName, ep);
        }
        outgoingMedia.release(new Continuation<Void>() {
            @Override
            public void onSuccess(Void result) throws Exception {
                log.info("PARTICIPANT {}: Released outgoing EP", UserSession.this.name);
            }

            @Override
            public void onError(Throwable cause) throws Exception {
                log.warn("USER {}: Could not release outgoing EP", UserSession.this.name);
            }
        });
    }

    private void makeIceJson(IceCandidateFoundEvent event, String username) {
        JsonObject candidate = JsonUtils.toJsonObject(event.getCandidate());
        JsonObject responseDto = new ICEDto
                .Response("iceCandidate", username, candidate)
                .toJson();

        try {
            sendMessage(responseDto);
        } catch (IOException e) {
            log.debug(e.getMessage());
        }
    }

    private void checkIfSuccess(String senderName, WebRtcEndpoint endpoint) {
        endpoint.release(new Continuation<>() {
            @Override
            public void onSuccess(Void result) throws Exception {
                log.info("PARTICIPANT {}: Released successfully incoming EP for {}",
                        UserSession.this.name, senderName);
            }

            @Override
            public void onError(Throwable cause) throws Exception {
                log.warn("PARTICIPANT {}: Could not release incoming EP for {}",
                        UserSession.this.name,
                        senderName);
            }
        });
    }
}
