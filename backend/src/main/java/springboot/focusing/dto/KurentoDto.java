package springboot.focusing.dto;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public interface KurentoDto {
    Gson gson = new Gson();

    default JsonObject toJson() {
        return gson.toJsonTree(this).getAsJsonObject();
    }
}
