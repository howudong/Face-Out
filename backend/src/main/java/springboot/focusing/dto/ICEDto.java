package springboot.focusing.dto;

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
        private final String candidate;
        private final String sdpMid;
        private final int sdpMLineIndex;
    }

    @Getter
    public static class CandidateDto {
        private String candidate;
        private String sdpMid;
        private int sdpMLineIndex;
    }
}
