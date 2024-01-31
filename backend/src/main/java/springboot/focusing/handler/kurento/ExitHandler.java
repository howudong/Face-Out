package springboot.focusing.handler.kurento;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.domain.UserSession;
import springboot.focusing.handler.KurentoHandler;
import springboot.focusing.service.UserRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ExitHandler implements KurentoHandler {
    @Override
    public void process(WebSocketSession session, UserRegistry registry, JsonObject jsonMessage) throws IOException {
        UserSession user = registry
                .findBySessionId(session.getId())
                .orElseThrow(IOException::new);

        log.debug("PARTICIPANT {}: exit ", user.getName());
        registry.removeBySession(user, session.getId());
        this.removeParticipant(registry, user.getName());
        user.close();
    }

    @Override
    public void onError() {
        log.error("ExitHandler : Error Occurred");
    }

    private void removeParticipant(UserRegistry registry, String name) throws IOException {
        log.debug("notifying all users that {} is leaving the room", name);

        final List<String> unnoticedParticipants = new ArrayList<>();
        final JsonObject participantLeftJson = new JsonObject();
        participantLeftJson.addProperty("id", "participantExit");
        participantLeftJson.addProperty("name", name);
        for (final UserSession participant : registry.getAllSession()) {
            try {
                participant.cancelVideoFrom(name);
                participant.sendMessage(participantLeftJson);
            } catch (final IOException e) {
                unnoticedParticipants.add(participant.getName());
            }
        }
        
        if (!unnoticedParticipants.isEmpty()) {
            log.debug("The users {} could not be notified that {} left the room",
                    unnoticedParticipants, name);
        }
    }
}
