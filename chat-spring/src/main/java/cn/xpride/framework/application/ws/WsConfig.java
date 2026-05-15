package cn.xpride.framework.application.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WsConfig implements WebSocketConfigurer {

    @Autowired
    private WsHandler wsHandler;
    @Autowired
    private WsHandShakeInterceptor wsHandShakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(wsHandler, "/im/webSocket").setAllowedOrigins("*")
            .addInterceptors(wsHandShakeInterceptor);
        registry.addHandler(wsHandler, "/im/webSocketJs").setAllowedOrigins("*")
            .addInterceptors(wsHandShakeInterceptor).withSockJS();
    }


}
