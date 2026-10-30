package cn.xpride.framework.base;

import cn.xpride.framework.application.handler.ChatHandler;
import io.github.jarvett.core.ioc.annotation.JuncoManaged;
import io.github.jarvett.web.*;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** 统一保留旧 HTTP 状态、内容类型和重复表单参数行为。 */
@JuncoManaged
public class CompatibilityFilter implements WebFilter {
    private final Map<String, String> methods = new LinkedHashMap<>();
    public CompatibilityFilter() {
        for (var method : ChatHandler.class.getDeclaredMethods()) {
            Path path = method.getAnnotation(Path.class);
            if (path != null) {
                methods.put("/im/" + path.value(), method.isAnnotationPresent(GET.class) ? "GET" : "POST");
            }
        }
    }
    @Override
    public void doFilter(JuncoHttpRequest request, JuncoHttpResponse response, WebFilterChain chain) {
        if (request.getPath().startsWith("/im/webSocketJs")) {
            chain.doFilter(request, response);
            return;
        }
        String expected = methods.get(request.getPath());
        if (expected == null) {
            response.setStatus(404);
            return;
        }
        response.setContentType("application/json");
        if (!expected.equals(request.getMethod())) {
            response.writeBody(ChatJson.write(new BaseResponse(405, "请求方式错误,只支持:[" + expected + "]")));
            return;
        }
        // Junco 参数 Map 只保留单值，合并重复 sidList，兼容表单的数组写法。
        String contentType = request.getHeader("Content-Type");
        if (contentType != null && contentType.startsWith("application/x-www-form-urlencoded")) {
            Map<String, String> values = new LinkedHashMap<>();
            for (String part : request.getBody().split("&")) {
                String[] pair = part.split("=", 2);
                if (pair.length == 2) {
                    String name = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                    values.merge(name, value, (a, b) -> name.equals("sidList") ? a + "," + b : a);
                }
            }
            values.forEach(request::addFormParameter);
        }
        chain.doFilter(request, response);
        response.setContentType("application/json");
    }
}
