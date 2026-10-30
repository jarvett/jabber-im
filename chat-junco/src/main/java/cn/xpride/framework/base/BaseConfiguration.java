package cn.xpride.framework.base;

import io.github.jarvett.core.ioc.annotation.JuncoBean;
import io.github.jarvett.core.ioc.annotation.JuncoConfig;
import cn.xpride.framework.application.ws.ChatUpgradeHandler;
import cn.xpride.framework.application.ws.WsHandler;
import cn.xpride.framework.application.ws.SockJsHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@JuncoConfig
public class BaseConfiguration implements AutoCloseable {
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    @JuncoBean
    public WsHandler wsHandler() {
        return new WsHandler();
    }
    @JuncoBean
    public SockJsHandler sockJsHandler(WsHandler handler) {
        return new SockJsHandler(handler);
    }
    /** 使用框架预留的升级处理器名称，替换为令牌与 SockJS 兼容适配器。 */
    @JuncoBean("juncoWebSocketUpgradeHandler")
    public ChatUpgradeHandler chatUpgradeHandler() {
        return new ChatUpgradeHandler();
    }

    /** 原线程池使用无界队列，实际并发为四个线程。 */
    @JuncoBean
    public ExecutorService threadPoolTaskExecutor() {
        return executor;
    }
    @Override
    public void close() {
        executor.shutdown();
    }
}

