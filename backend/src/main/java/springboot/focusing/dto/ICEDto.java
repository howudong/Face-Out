package springboot.focusing.dto;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public abstract class ICEDto implements KurentoDto {
    @Getter
    public static class Request extends ICEDto {
        private String id;
        private String name;
        private CandidateDto candidate;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Response extends ICEDto {
        private final String id;
        private final String name;
        private final JsonObject candidate;
    }

    @Getter
    public static class CandidateDto {
        private String candidate;
        private String sdpMid;
        private int sdpMLineIndex;

    }
}
