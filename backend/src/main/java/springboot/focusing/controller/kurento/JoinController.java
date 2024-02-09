package springboot.focusing.controller.kurento;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.MediaPipeline;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.controller.KurentoController;
import springboot.focusing.domain.UserSession;
import springboot.focusing.service.UserSessionService;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
@Controller
public class JoinController implements KurentoController {
    private final MediaPipeline pipeline;
    private final UserSessionService userService;

    @Override
    public void process(WebSocketSession session, JsonObject jsonMessage) throws IOException {
        UserSession user = createUserSession(session, jsonMessage);
        userService.register(session.getId(), user);
        notifyOthers(userService, user);
        sendParticipantNames(userService, user);
    }

    @Override
    public void onError() {
        log.error("JoinHandler : Error Occurred");
    }

    private UserSession createUserSession(WebSocketSession session, JsonObject jsonMessage) {
        String name = jsonMessage.get("name").getAsString();
        return new UserSession(pipeline, session, name);
    }

    private void notifyOthers(UserSessionService registry, UserSession newParticipant) {
        log.info("PARTICIPANT: trying to join room");
        final JsonObject newParticipantMsg = new JsonObject();
        newParticipantMsg.addProperty("id", "newParticipantArrived");
        newParticipantMsg.addProperty("name", newParticipant.getName());

        for (final UserSession participant : registry.findAllUserSession()) {
            try {
                if (!participant.getName().equals(newParticipant.getName())) {
                    log.info("notify to {} Msg : {} ", participant.getName(), newParticipantMsg);
                    participant.sendMessage(newParticipantMsg);
                }
            } catch (final IOException e) {
                log.info("ROOM {}: participant {} could not be notified",
                        newParticipant.getName(),
                        participant.getName(), e);
            }
        }
    }

    private void sendParticipantNames(UserSessionService registry, UserSession user) throws IOException {
        final JsonArray participantsArray = new JsonArray();
        for (final UserSession participant : registry.findAllUserSession()) {
            if (!participant.getName().equals(user.getName())) {
                final JsonElement participantName = new JsonPrimitive(participant.getName());
                participantsArray.add(participantName);
            }
        }

        final JsonObject existingParticipantsMsg = new JsonObject();
        existingParticipantsMsg.addProperty("id", "existingParticipants");
        existingParticipantsMsg.add("data", participantsArray);
        log.info("PARTICIPANT {}: sending a list of {} participants", user.getName(), participantsArray.size());
        user.sendMessage(existingParticipantsMsg);
    }
}
