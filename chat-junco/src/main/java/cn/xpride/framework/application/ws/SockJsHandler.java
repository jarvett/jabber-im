package cn.xpride.framework.application.ws;

import com.alibaba.fastjson.JSON;
import io.github.jarvett.websocket.*;
import lombok.RequiredArgsConstructor;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/** SockJS WebSocket 帧适配，实际连接和消息推送仍由 Junco 管理。 */
@WebSocketEndpoint("/im/webSocketJs/websocket")
@RequiredArgsConstructor
public class SockJsHandler implements WebSocketHandler {
    private final WsHandler handler;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void onOpen(WebSocketSession session) {
        boolean framed = Boolean.parseBoolean(session.getHeader("X-Chat-SockJS"));
        WebSocketSession adapted = framed ? new SockJsSession(session) : session;
        sessions.put(session.getId(), adapted);
        handler.onOpen(adapted);
        if (framed && session.isOpen()) {
            session.sendText("o");
        }
    }

    @Override
    public void onText(WebSocketSession session, String text) {
        WebSocketSession adapted = sessions.get(session.getId());
        if (adapted instanceof SockJsSession) {
            try {
                for (String message : JSON.parseArray(text, String.class)) {
                    handler.onText(adapted, message);
                }
            } catch (RuntimeException error) {
                session.close(1007, "Bad data");
            }
        } else {
            handler.onText(adapted, text);
        }
    }

    @Override
    public void onBinary(WebSocketSession session, byte[] message) {
        session.close(1000, "");
    }

    @Override
    public void onClose(WebSocketSession session, int status, String reason) {
        WebSocketSession adapted = sessions.remove(session.getId());
        if (adapted != null) {
            handler.onClose(adapted, status, reason);
        }
    }

    private record SockJsSession(WebSocketSession delegate) implements WebSocketSession {
        public String getId() { return delegate.getId(); }
        public String getPath() { return delegate.getPath(); }
        public String getHeader(String name) { return delegate.getHeader(name); }
        public String getQuery(String name) { return delegate.getQuery(name); }
        public boolean isOpen() { return delegate.isOpen(); }
        public void sendText(String message) { delegate.sendText("a" + JSON.toJSONString(List.of(message))); }
        public void sendBinary(byte[] message) { delegate.sendBinary(message); }
        public void close(int status, String reason) { delegate.close(status, reason); }
    }
}
