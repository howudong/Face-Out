package springboot.focusing.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

public abstract class JoinDto implements KurentoDto {
    @Getter
    public static class Request extends JoinDto {
        private String name;

    }

    @Getter
    @RequiredArgsConstructor
    public static class ExistingUserResponse extends JoinDto {
        private final String id;
        private final String name;
    }

    @Getter
    @RequiredArgsConstructor
    public static class NewUserResponse extends JoinDto {
        private final String id;
        private final List<String> data;
    }
}
