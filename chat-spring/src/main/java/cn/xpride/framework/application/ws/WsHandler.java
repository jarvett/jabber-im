package cn.xpride.framework.application.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WsHandler extends TextWebSocketHandler {

    private static final Map<Long, HashSet<String>> userMap = new HashMap<>();//一个用户登录多个会话的时候

    private static Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    private Long getUserId(WebSocketSession webSocketSession) {
        return (Long) webSocketSession.getAttributes().get("user_id");
    }

    private String getUserSessionId(WebSocketSession webSocketSession) {
        return (String) webSocketSession.getAttributes().get("user_session_id");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserId(session);
        String userSessionId = getUserSessionId(session);
        log.debug("[{} : {}]建立连接", userId, userSessionId);
        HashSet<String> sss = userMap.get(userId);
        if (null == sss) {
            sss = new HashSet<>();
        }
        sss.add(userSessionId);
        userMap.put(userId, sss);
        sessionMap.put(userSessionId, session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message.getPayload().equals("ping")) {//如果是心跳包,直接回应
            session.sendMessage(new TextMessage("pong"));
        } else {//ws只负责先客户端发送消息，除了心跳包,如果是收到其他信息,直接关闭
            session.close();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        Long userId = getUserId(session);
        String userSessionId = getUserSessionId(session);
        log.debug("[{} : {} 断开连接]", userId, userSessionId);
        HashSet<String> sss = userMap.get(userId);
        if (null != sss) {
            sss.remove(userSessionId);
        }
        sessionMap.remove(userSessionId);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 单发消息
     */
    public void sendMsgToUser(Long id, String data) {
        HashSet<String> sss = userMap.get(id);
        if (null == sss) return;
        for (String sid : sss) {
            WebSocketSession session = sessionMap.get(sid);
            TextMessage message = new TextMessage(data);
            try {
                session.sendMessage(message);
            } catch (IOException e) {
                log.warn("发送失败:[" + message.getPayload() + "]", e);
            }
        }
    }

    /**
     * 群发消息
     */
    public void broadCast(String data) {
        final TextMessage message = new TextMessage(data);
        for (Map.Entry<String, WebSocketSession> entry : sessionMap.entrySet()) {
            if (entry.getValue().isOpen()) {
                new Thread(() -> {
                    try {
                        if (entry.getValue().isOpen()) {
                            entry.getValue().sendMessage(message);
                        }
                    } catch (IOException e) {
                        log.error("发送出错", e);
                    }
                }).start();
            }
        }
    }

}
