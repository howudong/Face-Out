package springboot.focusing;

import springboot.focusing.handler.KurentoHandler;

import java.util.Map;

public class KurentoHandlerAdapter {
    private static final String NOT_MATCH_KEY = "error";
    private final Map<String, KurentoHandler> hanlderMap;

    public KurentoHandlerAdapter(Map<String, KurentoHandler> hanlderMap) {
        this.hanlderMap = hanlderMap;
    }

    public KurentoHandler getHandlerById(String id) {
        if (!hanlderMap.containsKey(id)) {
            id = NOT_MATCH_KEY;
        }
        return hanlderMap.get(id);
    }
}
