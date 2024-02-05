package springboot.focusing.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import springboot.focusing.domain.UserSession;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
public class MemoryUserSessionRepository implements UserSessionRepository {
    private final ConcurrentHashMap<String, UserSession> userByName = new ConcurrentHashMap<>();

    @Override
    public UserSession findBySessionId(String sessionId) {
        return userByName.values()
                .stream()
                .filter(session -> session.isSameSessionId(sessionId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public UserSession findByName(String name) {
        return (userByName.get(name));
    }

    @Override
    public List<UserSession> getAllSession() {
        return userByName.values()
                .stream()
                .toList();
    }

    @Override
    public UserSession add(String id, UserSession userSession) {
        log.info("register UserSession");
        log.info("Input UserName : {}", userSession.getName());
        userByName.put(userSession.getName(), userSession);
        return userSession;
    }

    @Override
    public boolean remove(String name) {
        if (userByName.containsKey(name)) {
            log.info("removeSession name {}", name);
            userByName.remove(name);
            return true;
        }
        return false;
    }
}
