package cn.xpride.framework.application.ws;

import cn.xpride.framework.base.MyToken;
import cn.xpride.framework.base.MyTokenUtil;
import io.github.jarvett.websocket.WebSocketEndpoint;
import io.github.jarvett.websocket.WebSocketHandler;
import io.github.jarvett.websocket.WebSocketSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 保留 ping/pong 和按用户广播，连接由 Junco WebSocket 管理。 */
@WebSocketEndpoint("/im/webSocket")
public class WsHandler implements WebSocketHandler {
    private final Map<WebSocketSession, Long> sessions = new ConcurrentHashMap<>();

    @Override
    public void onOpen(WebSocketSession session) {
        MyToken token = MyTokenUtil.getToken(session.getQuery("token"));
        if (token == null) {
            session.close(1008, "token无效请重新登录");
            return;
        }
        sessions.put(session, token.getUserId());
    }

    @Override
    public void onText(WebSocketSession session, String message) {
        if ("ping".equals(message)) {
            session.sendText("pong");
        } else {
            session.close(1000, "");
        }
    }

    @Override
    public void onBinary(WebSocketSession session, byte[] message) {
        session.close(1000, "");
    }

    @Override
    public void onClose(WebSocketSession session, int status, String reason) {
        sessions.remove(session);
    }

    public void sendMsgToUser(Long userId, String data) {
        sessions.forEach((session, id) -> {
            if (userId.equals(id) && session.isOpen()) {
                session.sendText(data);
            }
        });
    }
}
