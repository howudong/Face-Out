package springboot.focusing.service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.domain.UserSession;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
public class UserRegistry implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(UserRegistry.class);
    private final ConcurrentHashMap<String, UserSession> userBySessionId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UserSession> userByName = new ConcurrentHashMap<>();


    public void register(String id, UserSession userSession) {
        log.info("register UserSession");
        userBySessionId.put(id, userSession);
        userByName.put(userSession.getName(), userSession);
    }

    public UserSession findBySession(WebSocketSession session) {
        return userBySessionId.get(session.getId());
    }

    public UserSession findByName(String name) {
        return userByName.get(name);
    }

    public boolean isExist(String sessionId) {
        return userBySessionId.containsKey(sessionId);
    }

    public List<UserSession> getAllSession() {
        return userByName.values()
                .stream()
                .toList();
    }

    @Override
    public void close() {
        for (final UserSession user : userBySessionId.values()) {
            shutdown();
        }
    }

    @PreDestroy
    private void shutdown() {
        this.close();
    }

    public void removeBySession(WebSocketSession session) {
        UserSession removeSession = findBySession(session);
        userByName.remove(removeSession.getName());
        userBySessionId.remove(session.getId());
    }
}
