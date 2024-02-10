package springboot.focusing.controller.kurento;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.controller.KurentoController;
import springboot.focusing.domain.UserSession;
import springboot.focusing.dto.ICEDto;
import springboot.focusing.service.UserSessionService;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ICEController implements KurentoController {
    private final UserSessionService userService;

    @Override
    public void process(WebSocketSession session, JsonObject jsonMessage) {
        log.info("ICE Handler Process");
        ICEDto.Request requestDto = new ICEDto.Request().toDto(jsonMessage, ICEDto.Request.class);
        UserSession userSession = userService.findSession(session);
        IceCandidate candidate = makeIceCandidate(requestDto.getCandidate());
        userSession.addCandidate(candidate, requestDto.getName());
    }

    @Override
    public void onError() {
        log.error("ICEHandler : Error Occurred");
    }

    private IceCandidate makeIceCandidate(ICEDto.CandidateDto candidateDto) {

        log.info("MAKE ICE CANDIDATE {}", candidateDto);
        return new IceCandidate(
                candidateDto.getCandidate(),
                candidateDto.getSdpMid(),
                candidateDto.getSdpMLineIndex());
    }
}
