package cn.xpride.framework.base;

import cn.xpride.framework.application.ws.WsHandler;
import com.alibaba.fastjson.JSON;
import io.github.jarvett.core.ioc.annotation.JuncoManaged;
import io.github.jarvett.web.*;
import io.github.jarvett.websocket.WebSocketSession;
import java.util.*;
import java.util.concurrent.*;

/** SockJS HTTP 回退传输；异步挂起响应，不占用 Junco 的业务线程。 */
@JuncoManaged
public class SockJsHttp implements WebFilter, AutoCloseable {
    private final WsHandler handler;
    private final Map<String, PollSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();

    public SockJsHttp(WsHandler handler) {
        this.handler = handler;
        timer.scheduleAtFixedRate(this::expire, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public String[] includePaths() { return new String[]{"/im/webSocketJs/**", "/im/webSocketJs"}; }

    @Override
    public void doFilter(JuncoHttpRequest request, JuncoHttpResponse response, WebFilterChain chain) {
        String origin = request.getHeader("Origin");
        response.addHeader("Access-Control-Allow-Origin", origin == null ? "*" : origin);
        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        String path = request.getPath();
        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(204);
            response.addHeader("Access-Control-Allow-Methods", "OPTIONS, GET, POST");
            String headers = request.getHeader("Access-Control-Request-Headers");
            if (headers != null) {
                response.addHeader("Access-Control-Allow-Headers", headers);
            }
            return;
        }
        if (path.equals("/im/webSocketJs/info")) {
            response.setContentType("application/json;charset=UTF-8");
            response.writeBody("{\"websocket\":true,\"cookie_needed\":true,\"origins\":[\"*:*\"],\"entropy\":"
                    + ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE) + "}");
            return;
        }
        if (path.equals("/im/webSocketJs") || path.equals("/im/webSocketJs/")) {
            response.setContentType("text/plain;charset=UTF-8");
            response.writeBody("Welcome to SockJS!\n");
            return;
        }
        String[] parts = path.split("/");
        if (parts.length != 6 || !Set.of("xhr", "xhr_streaming", "xhr_send").contains(parts[5])) {
            response.setStatus(404);
            return;
        }
        if (!request.getMethod().equals("POST")) {
            response.setStatus(405);
            response.addHeader("Allow", "POST");
            return;
        }
        String token = request.getQueryParameter("token");
        if (MyTokenUtil.getToken(token) == null) {
            response.setStatus(403);
            return;
        }
        String id = parts[4];
        PollSession session = sessions.get(id);
        boolean send = parts[5].equals("xhr_send");
        if (session == null && send) {
            response.setStatus(404);
            return;
        }
        if (session == null) {
            PollSession candidate = new PollSession(id, token);
            session = sessions.putIfAbsent(id, candidate);
            if (session == null) {
                session = candidate;
                handler.onOpen(session);
            }
        }
        if (!session.token.equals(token)) {
            response.setStatus(403);
            return;
        }
        if (send) {
            try {
                List<String> messages = JSON.parseArray(request.getBody(), String.class);
                for (String message : messages) {
                    handler.onText(session, message);
                }
                response.setStatus(204);
            } catch (RuntimeException error) {
                response.setStatus(500);
                response.writeBody("Broken JSON encoding.");
            }
            return;
        }
        session.poll(response, parts[5].equals("xhr_streaming"));
    }

    private void expire() {
        long now = System.currentTimeMillis();
        sessions.forEach((id, session) -> {
            synchronized (session) {
                if (session.waiter == null && now - session.lastPoll > 5000) {
                    sessions.remove(id, session);
                    handler.onClose(session, 1000, "");
                }
            }
        });
    }

    @Override
    public void close() {
        timer.shutdownNow();
        sessions.values().forEach(session -> session.close(1001, "Server shutdown"));
        sessions.clear();
    }

    private final class PollSession implements WebSocketSession {
        private final String id;
        private final String token;
        private final Queue<String> frames = new ArrayDeque<>();
        private boolean open = true;
        private long lastPoll = System.currentTimeMillis();
        private JuncoHttpResponse response;
        private CompletableFuture<Void> waiter;
        private ScheduledFuture<?> heartbeat;
        private String prefix;

        PollSession(String id, String token) {
            this.id = id;
            this.token = token;
            frames.add("o");
        }
        synchronized void poll(JuncoHttpResponse target, boolean streaming) {
            target.setContentType("application/javascript;charset=UTF-8");
            if (waiter != null) {
                target.writeBody("c[2010,\"Another connection still open\"]\n");
                return;
            }
            lastPoll = System.currentTimeMillis();
            prefix = streaming ? "h".repeat(2048) + "\n" : "";
            if (!frames.isEmpty()) {
                target.writeBody(prefix + frames.remove() + "\n");
                return;
            }
            response = target;
            waiter = new CompletableFuture<>();
            target.defer(waiter, this::cancel);
            heartbeat = timer.schedule(() -> frame("h"), 25, TimeUnit.SECONDS);
        }
        private synchronized void cancel() {
            if (heartbeat != null) {
                heartbeat.cancel(false);
            }
            waiter = null;
            response = null;
            lastPoll = System.currentTimeMillis();
        }
        private synchronized void frame(String frame) {
            if (waiter == null) {
                frames.add(frame);
                return;
            }
            response.writeBody(prefix + frame + "\n");
            CompletableFuture<Void> completed = waiter;
            cancel();
            completed.complete(null);
        }
        public String getId() { return id; }
        public String getPath() { return "/im/webSocketJs"; }
        public String getHeader(String name) { return null; }
        public String getQuery(String name) { return name.equals("token") ? token : null; }
        public synchronized boolean isOpen() { return open; }
        public void sendText(String text) { frame("a" + JSON.toJSONString(List.of(text))); }
        public void sendBinary(byte[] bytes) { close(1003, "Binary unsupported"); }
        public synchronized void close(int status, String reason) {
            open = false;
            frame("c" + JSON.toJSONString(List.of(status, reason)));
            handler.onClose(this, status, reason);
        }
    }
}
