package springboot.focusing.dto;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

public abstract class JoinDto {
    private static final Gson gson = new Gson();

    @Getter
    public static class Request {
        private String name;

        public JsonObject toJson() {
            return gson.toJsonTree(this).getAsJsonObject();
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class ExistingUserResponse {
        private final String id;
        private final String name;

        public JsonObject toJson() {
            return gson.toJsonTree(this).getAsJsonObject();
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class NewUserResponse {
        private final String id;
        private final List<String> data;

        public JsonObject toJson() {
            return gson.toJsonTree(this).getAsJsonObject();
        }
    }
}
