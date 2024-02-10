package springboot.focusing.dto;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public interface KurentoDto {
    Gson gson = new Gson();

    default JsonObject toJson() {
        return gson.toJsonTree(this).getAsJsonObject();
    }

    default <T extends KurentoDto> T toDto(JsonObject jsonObject, Class<? extends T> targetClass) {
        return gson.fromJson(jsonObject, targetClass);
    }
}
