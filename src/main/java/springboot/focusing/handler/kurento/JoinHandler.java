package springboot.focusing.handler.kurento;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import springboot.focusing.domain.UserSession;
import springboot.focusing.handler.KurentoHandler;
import springboot.focusing.service.UserRegistry;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JoinHandler extends TextWebSocketHandler implements KurentoHandler {
    private final KurentoClient kurentoClient;

    @Override
    public void process(WebSocketSession session, UserRegistry registry, JsonObject jsonMessage) {
        UserSession user = createUserSession(session, jsonMessage);
        registry.register(session.getId(), user);
        notifyOthers(registry, user);
        sendParticipantNames(registry, user);
    }


    @Override
    public void onError() {
        //TODO
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

    }

    private UserSession createUserSession(WebSocketSession session, JsonObject jsonMessage) {
        String name = jsonMessage.get("name").getAsString();
        MediaPipeline pipeline = kurentoClient.createMediaPipeline();
        return new UserSession(pipeline, session, name);
    }

    private void notifyOthers(UserRegistry registry, UserSession newParticipant) {
        log.info("PARTICIPANT: trying to join room");
        final JsonObject newParticipantMsg = new JsonObject();
        newParticipantMsg.addProperty("id", "newParticipantArrived");
        newParticipantMsg.addProperty("name", newParticipant.getName());

        for (final UserSession participant : registry.getAllSession()) {
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

    private void sendParticipantNames(UserRegistry registry, UserSession user) {
        final JsonArray participantsArray = new JsonArray();
        for (final UserSession participant : registry.getAllSession()) {
            if (!participant.getName().equals(user.getName())) {
                final JsonElement participantName = new JsonPrimitive(participant.getName());
                participantsArray.add(participantName);
            }
        }

        final JsonObject existingParticipantsMsg = new JsonObject();
        existingParticipantsMsg.addProperty("id", "existingParticipants");
        existingParticipantsMsg.add("data", participantsArray);
        log.info("PARTICIPANT {}: sending a list of {} participants", user.getName(),
                participantsArray.size());
        try {
            user.sendMessage(existingParticipantsMsg);
        } catch (IOException e) {
        }
        ;
    }
}