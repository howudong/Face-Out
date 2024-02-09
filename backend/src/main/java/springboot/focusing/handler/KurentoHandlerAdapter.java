package springboot.focusing.handler;

import springboot.focusing.controller.KurentoController;

import java.util.Map;

public class KurentoHandlerAdapter {
    private static final String NOT_MATCH_ID = "error";
    private final Map<String, KurentoController> hanlderMap;

    public KurentoHandlerAdapter(Map<String, KurentoController> hanlderMap) {
        this.hanlderMap = hanlderMap;
    }

    public KurentoController findController(String id) {
        if (hanlderMap.get(id) == null) {
            id = NOT_MATCH_ID;
        }
        return hanlderMap.get(id);
    }

    public KurentoController getCloseController() {
        return findController(null);
    }
}
