package springboot.focusing.service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import springboot.focusing.domain.UserSession;

import java.io.Closeable;
import java.util.List;
import java.util.Optional;
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
        log.info("Input UserName : {}", userSession.getName());
        userByName.put(userSession.getName(), userSession);
    }

    public Optional<UserSession> findBySessionId(String sessionId) {
        return Optional.ofNullable(userBySessionId.get(sessionId));
    }

    public Optional<UserSession> findByName(String name) {
        return Optional.ofNullable(userByName.get(name));
    }

    public List<UserSession> getAllSession() {
        return userByName.values()
                .stream()
                .toList();
    }

    public void removeBySession(UserSession userSession, String sessionId) {
        log.info("removeSession name {}, id {}", userSession.getName(), sessionId);
        this.findByName(userSession.getName())
                .ifPresent(e -> userByName.remove(e.getName()));

        this.findBySessionId(sessionId)
                .ifPresent(e -> userBySessionId.remove(sessionId));
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
}
