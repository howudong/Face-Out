package springboot.focusing.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public abstract class ICEDto implements KurentoDto {
    @Getter
    @RequiredArgsConstructor
    public static class Request extends ICEDto {
        private String candidate;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Response extends ICEDto {
        private final String candidate;
        private final String sdpMid;
        private final int sdpMLineIndex;
    }
}
