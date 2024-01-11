package springboot.focusing.config;

import org.kurento.client.KurentoClient;
import org.kurento.jsonrpc.client.JsonRpcClientNettyWebSocket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import springboot.focusing.KurentoHandlerAdapter;
import springboot.focusing.handler.KurentoHandler;
import springboot.focusing.handler.MainHandler;
import springboot.focusing.handler.ReceiveVideoHandler;
import springboot.focusing.handler.kurento.ICEHandler;
import springboot.focusing.handler.kurento.JoinHandler;
import springboot.focusing.service.UserRegistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableWebSocket
public class KurentoConfig implements WebSocketConfigurer {
    // kms.url 를 application.properties 에 저장 후 사용
    @Value("${kms.url}")
    private String kmsUrl;

    @Bean
    public MainHandler createKurentoHandler() {
        KurentoHandlerAdapter kurentoHandlerAdapter = configKurentoHandler();
        return new MainHandler(kurentoHandlerAdapter);
    }

    @Bean
    public UserRegistry userRegistry() {
        return new UserRegistry();
    }

    private KurentoHandlerAdapter configKurentoHandler() {
        Map<String, KurentoHandler> handlerMap = new HashMap<>();
        handlerMap.put("join", new JoinHandler(userRegistry(), kurentoClient()));
        handlerMap.put("onIceCandidate", new ICEHandler(userRegistry()));
        handlerMap.put("receiveVideoFrom", new ReceiveVideoHandler());

        return new KurentoHandlerAdapter(Collections.unmodifiableMap(handlerMap));
    }

    /*
    Kurento Media Server 를 사용하기 위한 Bean 설정
    환경변수가 들어오면 환경변수를 KMS_URL 로 설정 or
    환경변수에 아무것도 안들어오면 application.properties 에 등록된 kms.url 을 가져와서 사용함
     */
    @Bean
    public KurentoClient kurentoClient() {
        String envKmsUrl = System.getenv("KMS_URL");
        if (Objects.isNull(envKmsUrl) || envKmsUrl.isEmpty()) {
            return createKurentoClient(kmsUrl);
        }
        return createKurentoClient(envKmsUrl);
    }

    private KurentoClient createKurentoClient(String url) {
        JsonRpcClientNettyWebSocket webSocket = new JsonRpcClientNettyWebSocket(url);
        return KurentoClient.createFromJsonRpcClient(webSocket);
    }

    /*
    웹 소켓에서 rtc 통신을 위한 최대 텍스트 버퍼와 바이너리 버퍼 사이즈를 설정
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(32768);
        container.setMaxBinaryMessageBufferSize(32768);
        return container;
    }

    // signal 로 요청이 왔을 때 아래의 WebSockerHandler 가 동작하도록 registry 에 설정
    // 요청은 클라이언트 접속, close, 메시지 발송 등에 대해 특정 메서드를 호출한다
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        System.out.println("KurentoConfig.registerWebSocketHandlers");
        registry.addHandler(createKurentoHandler(), "/signal").setAllowedOrigins("*");
    }
}
