package cn.xpride.framework;

import io.github.jarvett.core.JuncoApplication;
import io.github.jarvett.core.ioc.annotation.JuncoBootApplication;
import io.github.jarvett.core.ioc.annotation.JuncoScan;
import io.github.jarvett.jdbc.annotation.EnableJuncoJdbc;
import lombok.extern.slf4j.Slf4j;

/** 聊天服务的 Junco 启动入口。 */
@JuncoBootApplication
@EnableJuncoJdbc
// WebSocket 通过配置显式注册，以便使用握手认证适配器。
@JuncoScan({"cn.xpride.framework.base", "cn.xpride.framework.application.biz",
        "cn.xpride.framework.application.handler", "cn.xpride.framework.application.repository"})
@Slf4j
public class ChatJuncoApp {
    public static void main(String[] args) throws Exception {
        JuncoApplication.run(ChatJuncoApp.class);
        log.info("########################{}服务启动成功！########################", ChatJuncoApp.class.getName());
    }
}
