package springboot.focusing.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import springboot.focusing.KurentoHandlerAdapter;

import java.io.IOException;
import java.util.Optional;

public class KurentoMainHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(KurentoMainHandler.class);
    private static final Gson gson = new GsonBuilder().create();

    //private final UserRegistry registry;
    //private final KurentoClient kurentoClient;
    private final KurentoHandlerAdapter kurentoHandlerAdapter;

    public KurentoMainHandler(KurentoHandlerAdapter kurentoHandlerAdapter) {
        this.kurentoHandlerAdapter = kurentoHandlerAdapter;
    }


    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
        String id = jsonMessage.get("id").getAsString();

        Optional<KurentoHandler> kurentoHandler = kurentoHandlerAdapter.getHandlerById(id);
        if (kurentoHandler.isEmpty()) {
            sendError(session, "Invalid Message");
            return;
        }
        kurentoHandler.get().process(session, jsonMessage);
    }

    //            case "onIceCandidate": {
//                JsonObject jsonCandidate = jsonMessage.get("candidate").getAsJsonObject();
//                if (user != null) {
//                    IceCandidate candidate = new IceCandidate(jsonCandidate.get("candidate").getAsString(),
//                            jsonCandidate.get("sdpMid").getAsString(),
//                            jsonCandidate.get("sdpMLineIndex").getAsInt());
//                    user.addCandidate(candidate, session.getId());
//                }
//                break;
//            }
//        }
    private void sendError(WebSocketSession session, String message) {
        try {
            JsonObject response = new JsonObject();
            response.addProperty("id", "error");
            response.addProperty("message", message);
            session.sendMessage(new TextMessage(response.toString()));
        } catch (IOException e) {
            log.error("Exception sending message", e);
        }
    }
}
