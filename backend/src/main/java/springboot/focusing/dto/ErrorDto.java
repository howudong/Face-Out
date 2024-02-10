package springboot.focusing.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public abstract class ErrorDto implements KurentoDto {
    @Getter
    public static class Request extends ErrorDto {
        private String id;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Response extends ErrorDto {
        private final String id;
        private final String message;
    }
}
