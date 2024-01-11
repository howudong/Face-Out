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
        this.outgoingMedia = new WebRtcEndpoint.Builder(mediaPipeline)
                .useDataChannels()
                .build();
        this.outgoingMedia.addIceCandidateFoundListener(this::makeIceJson);

    }

    public void release() {
        this.mediaPipeline.release();
    }

    public void addCandidate(IceCandidate candidate, String sessionId) {
        if ((this.session.getId()).equals(sessionId)) {
            outgoingMedia.addIceCandidate(candidate);
            return;
        }
        WebRtcEndpoint webRtc = incomingMedia.get(sessionId);
        if (webRtc != null) {
            webRtc.addIceCandidate(candidate);
        }
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
            synchronized (session) {
                session.sendMessage(new TextMessage(response.toString()));
            }
        } catch (IOException e) {
            log.debug(e.getMessage());
        }
    }
}
