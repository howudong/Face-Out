package springboot.focusing.handler;

import springboot.focusing.controller.KurentoController;

import java.util.Map;

public class KurentoAdapter {
    private static final String NOT_MATCH_ID = "error";
    private static final String CLOSE_ID = "exit";
    private final Map<String, KurentoController> hanlderMap;

    public KurentoAdapter(Map<String, KurentoController> hanlderMap) {
        this.hanlderMap = hanlderMap;
    }

    public KurentoController findController(String id) {
        if (hanlderMap.get(id) == null) {
            id = NOT_MATCH_ID;
        }
        return hanlderMap.get(id);
    }

    public KurentoController getCloseController() {
        return findController(CLOSE_ID);
    }
}
