package springboot.focusing;

import springboot.focusing.handler.KurentoHandler;

import java.util.Map;
import java.util.Optional;

public class KurentoHandlerAdapter {
    private final Map<String, KurentoHandler> hanlderMap;

    public KurentoHandlerAdapter(Map<String, KurentoHandler> hanlderMap) {
        this.hanlderMap = hanlderMap;
    }

    public Optional<KurentoHandler> getHandlerById(String id) {
        return Optional.of(hanlderMap.get(id));
    }
}
