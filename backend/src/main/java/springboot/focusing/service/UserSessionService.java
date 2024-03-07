package springboot.focusing.service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import springboot.focusing.domain.UserSession;
import springboot.focusing.repository.UserSessionRepository;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserSessionService implements Closeable {
    private final UserSessionRepository userSessionRepository;

    public UserSession findSession(String username) throws NoSuchElementException {
        UserSession findUser = userSessionRepository.findByName(username);
        if (findUser == null) {
            log.error("Can't find User by name");
            throw new NoSuchElementException("CAN NOT FIND TARGET USER");
        }
        return findUser;
    }

    public UserSession findSession(WebSocketSession session) throws NoSuchElementException {
        UserSession findUser = userSessionRepository.findBySessionId(session.getId());
        if (findUser == null) {
            log.error("Can't find User by session");
            throw new NoSuchElementException("CAN NOT FIND TARGET USER");
        }
        return findUser;
    }

    public void removeSession(String name) throws NoSuchElementException {
        UserSession findUser = userSessionRepository.findByName(name);
        if (findUser == null) {
            log.error("Can't find User by session");
            throw new NoSuchElementException("CAN NOT FIND TARGET USER");
        }
        userSessionRepository.remove(name);
    }

    public void register(String id, UserSession user) {
        log.info("new User Registered id : {} , user : {}", id, user);
        userSessionRepository.add(id, user);
    }

    public List<UserSession> findAllUserSession() {
        return userSessionRepository.getAllSession();
    }

    @Override
    public void close() throws IOException {
        for (final UserSession user : userSessionRepository.getAllSession()) {
            user.close();
        }
    }

    @PreDestroy
    private void shutdown() throws IOException {
        log.info("some UserSession ShutDown");
        this.close();
    }
}
