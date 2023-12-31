package focusing.service;

import focusing.domain.UserSession;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.Closeable;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
public class UserRegistry implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(UserRegistry.class);
    private final ConcurrentHashMap<String, UserSession> users = new ConcurrentHashMap<>();

    public void register(String id, UserSession userSession) {
        System.out.println("register UserSession");
        log.info("register UserSession");
        users.put(id, userSession);
    }

    public UserSession findBySessionId(String sessionId) {
        return users.get(sessionId);
    }

    public boolean isExist(String sessionId) {
        return users.containsKey(sessionId);
    }

    @Override
    public void close() {
        for (final UserSession user : users.values()) {
            shutdown();
        }
    }

    @PreDestroy
    private void shutdown() {
        this.close();
    }

    public void removeBySession(WebSocketSession session) {
        users.remove(session.getId());
    }
}
