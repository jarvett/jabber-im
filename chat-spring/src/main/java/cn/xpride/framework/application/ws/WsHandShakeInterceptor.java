package cn.xpride.framework.application.ws;

import cn.xpride.framework.base.MyToken;
import cn.xpride.framework.base.MyTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

@Component
@Slf4j
public class WsHandShakeInterceptor extends HttpSessionHandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        ServletServerHttpRequest hsr = (ServletServerHttpRequest) request;
        String token = hsr.getServletRequest().getParameter("token");
        MyToken model = MyTokenUtil.getToken(token);
        if (null == model) {
            log.debug("token无效请重新登录");
            return false;
        } else {
            attributes.put("user_id", model.getUserId());
            attributes.put("user_session_id", token);
            return true;
        }
    }

}
