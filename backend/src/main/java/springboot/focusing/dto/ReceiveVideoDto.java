package springboot.focusing.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public abstract class ReceiveVideoDto implements KurentoDto {
    @Getter
    public static class Request extends ReceiveVideoDto {
        private String id;
        private String sender;
        private String sdpOffer;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Response extends ReceiveVideoDto {
        private final String id;
        private final String name;
        private final String sdpAnswer;
    }
}
