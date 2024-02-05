package springboot.focusing.repository;

import springboot.focusing.domain.UserSession;

import java.util.List;

public interface UserSessionRepository {
    UserSession findBySessionId(String sessionId);

    UserSession findByName(String name);

    List<UserSession> getAllSession();

    UserSession add(String id, UserSession userSession);

    boolean remove(String name);
}
