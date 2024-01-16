package springboot.focusing;

import springboot.focusing.handler.KurentoHandler;

import java.util.Map;

public class KurentoHandlerAdapter {
    private static final String NOT_MATCH_ID = "error";
    private final Map<String, KurentoHandler> hanlderMap;

    public KurentoHandlerAdapter(Map<String, KurentoHandler> hanlderMap) {
        this.hanlderMap = hanlderMap;
    }

    public KurentoHandler findHandlerById(String id) {
        if (hanlderMap.get(id) == null) {
            id = NOT_MATCH_ID;
        }
        return hanlderMap.get(id);
    }
}
