package springboot.focusing.domain;

import com.google.gson.JsonObject;
import org.kurento.client.*;
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

    public WebRtcEndpoint getOutgoingMedia() {
        return outgoingMedia;
    }

    public void connectPeer(UserSession sender, WebRtcEndpoint incoming) {
        sender.getOutgoingMedia().connect(incoming, new Continuation<Void>() {
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

    public WebRtcEndpoint getEndpointForUser(final UserSession sender) {
        if (sender.getName().equals(name)) {
            log.info("PARTICIPANT {}: configuring loopback", this.name);
            return outgoingMedia;
        }

        log.info("PARTICIPANT {}: receiving video from {}", this.name, sender.getName());

        WebRtcEndpoint incoming = incomingMedia.get(sender.getName());
        if (incoming == null) {
            log.info("PARTICIPANT {}: creating new endpoint for {}", this.name, sender.getName());
            incoming = new WebRtcEndpoint.Builder(mediaPipeline).useDataChannels().build();
            incoming.addIceCandidateFoundListener(event -> makeIceJson(event, sender.getName()));
            incomingMedia.put(sender.getName(), incoming);
        }

        log.info("PARTICIPANT {}: obtained endpoint for {}", this.name, sender.getName());
        connectPeer(sender, incoming);
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
        incoming.release(new Continuation<Void>() {
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

    public String getName() {
        return name;
    }
    public boolean isSameSessionId(String sessionId){
        return this.session
                .getId()
                .equals(sessionId);
    }

    @Override
    public void close() throws IOException {
        log.debug("PARTICIPANT {}: Releasing resources", this.name);
        for (final String remoteParticipantName : incomingMedia.keySet()) {
            log.trace("PARTICIPANT {}: Released incoming EP for {}", this.name, remoteParticipantName);
            final WebRtcEndpoint ep = this.incomingMedia.get(remoteParticipantName);

            ep.release(new Continuation<Void>() {
                @Override
                public void onSuccess(Void result) throws Exception {
                    log.info("PARTICIPANT {}: Released successfully incoming EP for {}",
                            UserSession.this.name, remoteParticipantName);
                }

                @Override
                public void onError(Throwable cause) throws Exception {
                    log.warn("PARTICIPANT {}: Could not release incoming EP for {}", UserSession.this.name,
                            remoteParticipantName);
                }
            });
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
        JsonObject response = new JsonObject();
        response.addProperty("id", "iceCandidate");
        response.addProperty("name", username);
        response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(response.toString()));
            }
        } catch (IOException e) {
            log.debug(e.getMessage());
        }
    }
}
