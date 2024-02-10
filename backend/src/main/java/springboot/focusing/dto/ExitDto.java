package springboot.focusing.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public abstract class ExitDto implements KurentoDto {
    @Getter
    @RequiredArgsConstructor
    public static class Request extends ExitDto {
        private String id;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Response extends ExitDto {
        private final String id;
        private final String name;
    }
}
