package springboot.focusing.handler.kurento;

import com.google.gson.JsonObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.handler.KurentoHandler;
import springboot.focusing.service.UserRegistry;

import java.io.IOException;

public class ErrorHandler implements KurentoHandler {
    @Override
    public void process(WebSocketSession session, UserRegistry registry, JsonObject jsonMessage) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("id", "error");
        response.addProperty("message", "Invalid Message");
        session.sendMessage(new TextMessage(response.toString()));
    }

    @Override
    public void onError() {

    }
}
