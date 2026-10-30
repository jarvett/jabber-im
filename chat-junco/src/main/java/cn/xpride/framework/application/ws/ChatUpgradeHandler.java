package cn.xpride.framework.application.ws;

import cn.xpride.framework.base.MyTokenUtil;
import io.github.jarvett.websocket.internal.NettyWebSocketUpgradeHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import java.util.List;
import java.util.concurrent.Executor;

/** 在协议升级前校验令牌，并将 SockJS 动态路径交给 Junco WebSocket。 */
public class ChatUpgradeHandler extends NettyWebSocketUpgradeHandler {
    @Override
    public boolean supports(HttpRequest request) {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        String path = decoder.path();
        // 在 Junco 把查询参数压成单值之前，保留 Spring 支持的重复 sidList。
        if (path.startsWith("/im/") && decoder.parameters().getOrDefault("sidList", List.of()).size() > 1) {
            QueryStringEncoder encoder = new QueryStringEncoder(path);
            decoder.parameters().forEach((key, values) -> {
                if (key.equals("sidList")) {
                    encoder.addParam(key, String.join(",", values));
                } else {
                    values.forEach(value -> encoder.addParam(key, value));
                }
            });
            request.setUri(encoder.toString());
        }
        return "websocket".equalsIgnoreCase(request.headers().get("Upgrade"))
                && (path.equals("/im/webSocket") || path.equals("/im/webSocketJs/websocket")
                || path.matches("/im/webSocketJs/[^/.]+/[^/.]+/websocket"));
    }

    @Override
    public void upgrade(ChannelHandlerContext context, HttpRequest request, Executor executor) {
        QueryStringDecoder query = new QueryStringDecoder(request.uri());
        String token = query.parameters().getOrDefault("token", List.of("")).get(0);
        if (MyTokenUtil.getToken(token) == null) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN);
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, 0);
            context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        if (query.path().startsWith("/im/webSocketJs/")) {
            String rawQuery = request.uri().contains("?") ? request.uri().substring(request.uri().indexOf('?')) : "";
            // /websocket 是 SockJS 的原始 WebSocket 入口；动态会话入口才需要帧封装。
            request.headers().set("X-Chat-SockJS", !query.path().equals("/im/webSocketJs/websocket"));
            request.setUri("/im/webSocketJs/websocket" + rawQuery);
        }
        super.upgrade(context, request, executor);
    }
}
