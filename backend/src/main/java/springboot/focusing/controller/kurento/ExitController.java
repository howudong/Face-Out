package springboot.focusing.controller.kurento;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.controller.KurentoController;
import springboot.focusing.domain.UserSession;
import springboot.focusing.service.UserSessionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ExitController implements KurentoController {
    private final UserSessionService userService;

    @Override
    public void process(WebSocketSession session, JsonObject jsonMessage) throws IOException {
        UserSession user = userService.findSession(session);
        log.debug("PARTICIPANT {}: exit ", user.getName());
        userService.removeSession(user.getName());
        this.removeParticipant(userService, user.getName());
        user.close();
    }

    @Override
    public void onError() {
        log.error("ExitHandler : Error Occurred");
    }

    private void removeParticipant(UserSessionService registry, String name) throws IOException {
        log.debug("notifying all users that {} is leaving the room", name);

        final List<String> unnoticedParticipants = new ArrayList<>();
        final JsonObject participantLeftJson = new JsonObject();
        participantLeftJson.addProperty("id", "participantExit");
        participantLeftJson.addProperty("name", name);
        for (final UserSession participant : registry.findAllUserSession()) {
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
