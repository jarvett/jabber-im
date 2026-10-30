package cn.xpride.framework;

import cn.xpride.framework.application.biz.ChatBizService;
import cn.xpride.framework.application.model.*;
import cn.xpride.framework.base.HandlerException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import static org.junit.Assert.*;

/** 真实 JPA 事务验证；不连接业务 MySQL。 */
@RunWith(SpringRunner.class)
@DataJpaTest(properties = {"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect", "spring.jpa.show-sql=false"})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({ChatBizService.class, ReadReceiptTest.Config.class})
public class ReadReceiptTest {
    @Autowired private ChatBizService service;
    @TestConfiguration
    static class Config {
        @Bean public ThreadPoolTaskExecutor threadPoolTaskExecutor() { return new ThreadPoolTaskExecutor(); }
    }
    @Test
    public void readReceiptClearsOnlyCurrentUserAndLatestMessage() throws Exception {
        Long a = service.findOrCreateSubscriberByPhone("13800138000");
        Long b = service.findOrCreateSubscriberByPhone("13800138001");
        Long outsider = service.findOrCreateSubscriberByPhone("13800138002");
        Long channel = service.findOrCreatePrivateDialogue(a,b).getChannelId();
        MessageContentRequest request = new MessageContentRequest();
        request.setChannelId(channel);request.setSenderId(a);
        MessageContentModel content = new MessageContentModel();content.setType("text");content.setText("first");request.setContentModel(content);
        String first = service.sendMsgToChannel(request).getId();
        assertEquals(Integer.valueOf(1), service.getDialogueByChannelId(b,channel).getUnreadCount());
        assertEquals(Integer.valueOf(0), service.markRead(b,channel,first).getUnreadCount());
        assertEquals(Integer.valueOf(0), service.markRead(b,channel,first).getUnreadCount());
        String next = service.sendMsgToChannel(request).getId();
        assertEquals(Integer.valueOf(1), service.markRead(b,channel,first).getUnreadCount());
        assertEquals(Integer.valueOf(0), service.markRead(b,channel,next).getUnreadCount());
        try { service.markRead(outsider,channel,next); fail("非成员不应读取会话"); }
        catch (HandlerException expected) { assertEquals(10, expected.getCode()); }
    }
}
