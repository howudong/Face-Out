package springboot.focusing.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class JoinDto {
    public static class Request {
        private String name;
    }

    public static class ExistingUserResponse {
        private String id;
        private String name;
    }

    public static class NewUserResponse {
        private String id;
        private List<String> names;
    }
}
