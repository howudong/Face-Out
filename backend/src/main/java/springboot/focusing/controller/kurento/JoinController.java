package springboot.focusing.controller.kurento;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.MediaPipeline;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.controller.KurentoController;
import springboot.focusing.domain.UserSession;
import springboot.focusing.dto.JoinDto;
import springboot.focusing.service.UserSessionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        JoinDto.Request requestDto = new JoinDto
                .Request()
                .toDto(jsonMessage, JoinDto.Request.class);
        
        return new UserSession(pipeline, session, requestDto.getName());
    }

    private void notifyOthers(UserSessionService registry, UserSession newParticipant) {
        log.info("PARTICIPANT: trying to join room");
        JsonObject newParticipantMsg = createResponse(newParticipant.getName());
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
        List<String> usernames = new ArrayList<>();
        for (final UserSession participant : registry.findAllUserSession()) {
            if (!participant.getName().equals(user.getName())) {
                usernames.add(participant.getName());
            }
        }
        JsonObject existingParticipantsMsg = createResponse(usernames.toArray(new String[0]));
        log.info("PARTICIPANT {}: sending a list of {} participants", user.getName(), usernames.size());
        user.sendMessage(existingParticipantsMsg);
    }

    private JsonObject createResponse(String... usernames) {
        if (usernames.length == 1) {
            return getNewParticipantDto(usernames);
        }
        return getExistingParticipantDto(usernames);
    }

    private JsonObject getNewParticipantDto(String[] usernames) {
        return new JoinDto
                .ExistingUserResponse("newParticipantArrived", usernames[0])
                .toJson();
    }

    private JsonObject getExistingParticipantDto(String[] usernames) {
        return new JoinDto
                .NewUserResponse("existingParticipants", List.of(usernames))
                .toJson();
    }
}
