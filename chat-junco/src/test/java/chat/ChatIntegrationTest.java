package chat;

import cn.xpride.framework.ChatJuncoApp;
import cn.xpride.framework.base.MyTokenUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.jarvett.jdbc.core.JdbcTemplate;
import io.github.jarvett.test.annotation.JuncoTest;
import io.github.jarvett.test.annotation.WebEnvironment;
import io.github.jarvett.test.core.TestContext;
import org.junit.jupiter.api.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;

/** 使用真实 Junco 容器、HTTP/WebSocket 和 H2 MySQL 模式验证完整聊天链路。 */
@JuncoTest(classes = ChatJuncoApp.class, webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
        "jdbc.url=jdbc:h2:mem:chat;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "jdbc.username=sa", "jdbc.password=test", "jdbc.driver-class-name=org.h2.Driver",
        "jdbc.pool.min-idle=1", "jdbc.pool.max-size=4", "logging.level.root=ERROR"})
public class ChatIntegrationTest {
    TestContext ctx;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @BeforeEach
    void schema() throws Exception {
        // 总 SQL 是唯一表结构来源，仅去掉 H2 不支持的 MySQL 排序规则和索引存储声明。
        String ddl = Files.readString(Path.of("../sql/chat-mysql.sql"), StandardCharsets.UTF_8)
                .replaceAll("(?s)/\\*.*?\\*/", "")
                .replaceAll("(?m)^--.*$", "")
                .replaceAll(" COLLATE utf8mb4_unicode_ci", "")
                .replaceAll(" USING BTREE", "")
                .replaceAll("\\) ENGINE=InnoDB[^;]*", ")");
        JdbcTemplate jdbc = ctx.getBean(JdbcTemplate.class);
        for (String statement : ddl.split(";")) {
            if (!statement.isBlank() && !statement.contains("FOREIGN_KEY_CHECKS")) {
                jdbc.update(statement);
            }
        }
    }

    JSONObject request(String method, String path, String token, String form) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create(ctx.getWebBaseUrl() + "/im/" + path))
                .timeout(Duration.ofSeconds(10));
        if (token != null) {
            builder.header("token", token);
        }
        if (method.equals("POST")) {
            builder.header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form == null ? "" : form));
        } else {
            builder.GET();
        }
        var response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), response.body());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
        return JSON.parseObject(response.body());
    }

    JSONObject login(String phone) throws Exception {
        JSONObject result = request("GET", "getToken?phone=" + phone + "&code=123456", null, null);
        assertEquals(0, result.getIntValue("code"), result.toString());
        assertInstanceOf(String.class, result.get("sid"));
        assertInstanceOf(String.class, result.get("expiresAtStamp"));
        assertTrue(result.containsKey("msg"));
        assertNull(result.get("msg"));
        assertEquals(result.getLong("sid"), MyTokenUtil.getToken(result.getString("token")).getUserId());
        return result;
    }

    @Test
    void errorsAndTokenSources() throws Exception {
        assertEquals(400, request("GET", "getPhoneCode", null, null).getIntValue("code"));
        assertEquals("手机号格式错误", request("GET", "getPhoneCode?phone=no", null, null).getString("msg"));
        assertEquals("手机验证码错误", request("GET", "getToken?phone=13800138000&code=0", null, null).getString("msg"));
        assertEquals(666, request("GET", "getMyProfile", null, null).getIntValue("code"));
        assertEquals(405, request("POST", "getMyProfile", null, "").getIntValue("code"));
        JSONObject user = login("13800138000");
        String token = user.getString("token");
        assertEquals(0, request("GET", "getMyProfile?token=" + token, null, null).getIntValue("code"));
        var cookie = client.send(HttpRequest.newBuilder(URI.create(ctx.getWebBaseUrl() + "/im/getMyProfile"))
                .header("Cookie", "token=" + token).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(0, JSON.parseObject(cookie.body()).getIntValue("code"));
        assertEquals(400, request("POST", "createPrivateDialogue", token, "sid=no").getIntValue("code"));
        assertEquals(400, request("GET", "getMyDialogueList?pageNow=x", token, null).getIntValue("code"));
        assertEquals("消息体类型不能为空", request("POST", "sendMsgToChannel", token, "channelId=1").getString("msg"));
    }

    @Test
    void privateAndGroupChat() throws Exception {
        assertEquals("138****8000", request("GET", "getPhoneCode?phone=13800138000", null, null).getString("phone"));
        JSONObject a = login("13800138000");
        JSONObject b = login("13800138001");
        JSONObject c = login("13800138002");
        String token = a.getString("token");
        assertEquals(a.getString("sid"), login("13800138000").getString("sid"));
        assertEquals(0, request("GET", "getMyProfile", token, null).getIntValue("code"));
        assertEquals(b.getString("sid"), request("GET", "getMyProfileByPhone?phone=13800138001", token, null)
                .getJSONObject("profile").getString("sid"));
        assertEquals(0, request("POST", "changeIntro", token, "intro=hello").getIntValue("code"));
        assertEquals("hello", request("GET", "getMyProfile", token, null).getJSONObject("profile").getString("intro"));
        assertEquals(0, request("POST", "changeAvatarAndNickName", token, "").getIntValue("code"));
        assertEquals(2, request("GET", "getRandomStranger", token, null).getJSONArray("subscriberList").size());
        assertEquals(10, request("POST", "createPrivateDialogue", token, "sid=" + a.getString("sid")).getIntValue("code"));
        assertEquals(10, request("POST", "createPrivateDialogue", token, "sid=999").getIntValue("code"));
        JSONObject dialogue = request("POST", "createPrivateDialogue", token, "sid=" + b.getString("sid")).getJSONObject("model");
        String channel = dialogue.getString("channelId");
        assertEquals(channel, request("POST", "createPrivateDialogue", token, "sid=" + b.getString("sid")).getJSONObject("model").getString("channelId"));
        assertEquals(1, request("GET", "getMyDialogueList", token, null).getJSONArray("dialogueList").size());
        assertEquals(channel, request("GET", "getDialogueByChannelId?channelId=" + channel, token, null).getJSONObject("model").getString("channelId"));
        assertEquals(2, request("GET", "getSubscriberModelListByChannelId?channelId=" + channel, token, null).getJSONArray("subscriberList").size());
        assertEquals(10, request("POST", "sendMsgToChannel", c.getString("token"), "channelId=" + channel + "&type=text&text=forbidden").getIntValue("code"));
        assertEquals(10, request("POST", "sendMsgToChannel", token, "channelId=" + channel + "&type=text").getIntValue("code"));
        assertEquals(10, request("POST", "sendMsgToChannel", token, "channelId=" + channel + "&type=event").getIntValue("code"));
        Messages listener = new Messages();
        WebSocket socket = connect("/im/webSocket?token=" + b.getString("token"), listener);
        socket.sendText("ping", true).join();
        assertEquals("pong", listener.next());
        assertEquals(0, request("POST", "sendMsgToChannel", token, "channelId=" + channel + "&type=text&text=hello").getIntValue("code"));
        JSONObject push = JSON.parseObject(listener.next());
        assertEquals("hello", push.getJSONObject("content").getString("text"));
        assertFalse(push.containsKey("receiveIdList"));
        assertEquals(1, request("GET", "getMyDialogueList", b.getString("token"), null).getJSONArray("dialogueList").getJSONObject(0).getIntValue("unreadCount"));
        assertEquals(0, request("POST", "sendMsgToChannel", b.getString("token"), "channelId=" + channel + "&type=image&imageUrl=test.png").getIntValue("code"));
        assertNotNull(listener.next());
        var history = request("GET", "getMsgListByChannelId?channelId=" + channel, token, null).getJSONArray("channelMsgList");
        assertEquals(2, history.size());
        assertInstanceOf(String.class, history.getJSONObject(0).get("time"));
        assertEquals(0, request("GET", "getMsgListByChannelId?channelId=" + channel + "&pageNow=2", token, null).getJSONArray("channelMsgList").size());
        socket.sendClose(1000, "done").join();

        // 使用本地图片让群头像合成确定可测，不访问随机头像服务。
        Path avatar = Path.of("target/test-avatar.png");
        ImageIO.write(new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB), "png", avatar.toFile());
        JdbcTemplate jdbc = ctx.getBean(JdbcTemplate.class);
        jdbc.update("update im_subscriber set avatar = ?", avatar.toAbsolutePath().toString());
        JSONObject group = request("POST", "createGroupDialogue", token,
                "sidList=" + b.getString("sid") + "&sidList=" + c.getString("sid") + "&groupName=team");
        assertEquals(0, group.getIntValue("code"), group.toString());
        String groupId = group.getJSONObject("model").getString("channelId");
        assertEquals(0, request("POST", "sendMsgToChannel", token, "channelId=" + groupId + "&type=text&text=group").getIntValue("code"));
        assertEquals(3, request("GET", "getSubscriberModelListByChannelId?channelId=" + groupId, token, null).getJSONArray("subscriberList").size());
        assertEquals(1, jdbc.queryValue("select unread_count from im_dialogue where subscriber_id = ? and channel_id = ?", Integer.class, c.getLong("sid"), Long.valueOf(groupId)));
        String groupMessage = request("GET", "getMsgListByChannelId?channelId=" + groupId, c.getString("token"), null).getJSONArray("channelMsgList").getJSONObject(0).getString("id");
        assertEquals(10, request("POST", "markRead", c.getString("token"), "channelId=" + groupId + "&messageId=" + history.getJSONObject(0).getString("id")).getIntValue("code"));
        assertEquals(0, request("POST", "markRead", c.getString("token"), "channelId=" + groupId + "&messageId=" + groupMessage).getJSONObject("model").getIntValue("unreadCount"));
        assertEquals(1, jdbc.queryValue("select unread_count from im_dialogue where subscriber_id = ? and channel_id = ?", Integer.class, b.getLong("sid"), Long.valueOf(groupId)));
        String lockPlan = jdbc.queryValue("explain select * from im_channel where id = ? for update", String.class, Long.valueOf(groupId));
        assertTrue(lockPlan.toUpperCase().contains("PRIMARY_KEY"), lockPlan);
        JSONObject queryGroup = request("POST", "createGroupDialogue?sidList=" + b.getString("sid")
                + "&sidList=" + c.getString("sid") + "&groupName=query", token, "");
        assertEquals(0, queryGroup.getIntValue("code"), queryGroup.toString());
        assertEquals(1, jdbc.queryValue("select msg_count from im_channel where id = ?", Integer.class, Long.valueOf(groupId)));
    }

    @Test
    void websocketHandshakeAndSockJsFrames() throws Exception {
        assertThrows(CompletionException.class, () -> connect("/im/webSocket?token=bad", new Messages()));
        String token = login("13800138000").getString("token");
        assertTrue(request("GET", "webSocketJs/info", null, null).getBooleanValue("websocket"));
        Messages listener = new Messages();
        WebSocket socket = connect("/im/webSocketJs/001/session/websocket?token=" + token, listener);
        assertEquals("o", listener.next());
        socket.sendText("[\"ping\"]", true).join();
        assertEquals("a[\"pong\"]", listener.next());
        socket.sendClose(1000, "done").join();
    }

    @Test
    void sockJsPollingAndStreaming() throws Exception {
        JSONObject user = login("13800138000");
        String base = ctx.getWebBaseUrl() + "/im/webSocketJs/001/poll-session/";
        String suffix = "?token=" + user.getString("token");
        var open = client.send(HttpRequest.newBuilder(URI.create(base + "xhr" + suffix))
                .POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, open.statusCode());
        assertEquals("o\n", open.body());
        var poll = client.sendAsync(HttpRequest.newBuilder(URI.create(base + "xhr_streaming" + suffix))
                .POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
        var sent = client.send(HttpRequest.newBuilder(URI.create(base + "xhr_send" + suffix))
                .POST(HttpRequest.BodyPublishers.ofString("[\"ping\"]")).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(204, sent.statusCode());
        assertTrue(poll.get(5, TimeUnit.SECONDS).body().endsWith("a[\"pong\"]\n"));
        String other = login("13800138001").getString("token");
        var forbidden = client.send(HttpRequest.newBuilder(URI.create(base + "xhr?token=" + other))
                .POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(403, forbidden.statusCode());
    }

    @Test
    void readReceiptIsAuthorizedIdempotentAndRejectsOldSnapshots() throws Exception {
        JSONObject a = login("13800138000"), b = login("13800138001"), c = login("13800138002");
        String channel = request("POST", "createPrivateDialogue", a.getString("token"), "sid=" + b.getString("sid")).getJSONObject("model").getString("channelId");
        request("POST", "sendMsgToChannel", a.getString("token"), "channelId=" + channel + "&type=text&text=first");
        String first = request("GET", "getMsgListByChannelId?channelId=" + channel, b.getString("token"), null).getJSONArray("channelMsgList").getJSONObject(0).getString("id");
        assertEquals(1, request("GET", "getDialogueByChannelId?channelId=" + channel, b.getString("token"), null).getJSONObject("model").getIntValue("unreadCount"));
        String form = "channelId=" + channel + "&messageId=" + first;
        assertEquals(10, request("POST", "markRead", c.getString("token"), form).getIntValue("code"));
        assertEquals(666, request("POST", "markRead", null, form).getIntValue("code"));
        assertEquals(10, request("POST", "markRead", b.getString("token"), "channelId=" + channel + "&messageId=missing").getIntValue("code"));
        JSONObject before = request("GET", "getDialogueByChannelId?channelId=" + channel, b.getString("token"), null).getJSONObject("model");
        assertEquals(0, request("POST", "markRead", b.getString("token"), form).getJSONObject("model").getIntValue("unreadCount"));
        assertEquals(0, request("POST", "markRead", b.getString("token"), form).getJSONObject("model").getIntValue("unreadCount"));
        JSONObject after = request("GET", "getDialogueByChannelId?channelId=" + channel, b.getString("token"), null).getJSONObject("model");
        assertEquals(before.getString("brief"), after.getString("brief"));
        assertEquals(before.getString("lastTime"), after.getString("lastTime"));
        request("POST", "sendMsgToChannel", a.getString("token"), "channelId=" + channel + "&type=text&text=second");
        // 模拟同毫秒两条消息，不能仅根据时间戳确认已读。
        JdbcTemplate jdbc = ctx.getBean(JdbcTemplate.class);
        jdbc.update("update im_channel_msg set time = 100 where channel_id = ?", Long.valueOf(channel));
        assertEquals(1, request("POST", "markRead", b.getString("token"), form).getJSONObject("model").getIntValue("unreadCount"));
        String latest = request("GET", "getMsgListByChannelId?channelId=" + channel, b.getString("token"), null).getJSONArray("channelMsgList").getJSONObject(0).getString("id");
        assertNotEquals(first, latest);
        assertEquals(0, request("POST", "markRead", b.getString("token"), "channelId=" + channel + "&messageId=" + latest).getJSONObject("model").getIntValue("unreadCount"));
        // 发送末尾失败必须回滚前面的未读数和消息插入。
        jdbc.update("update im_channel set msg_count = 127 where id = ?", Long.valueOf(channel));
        assertEquals(500, request("POST", "sendMsgToChannel", a.getString("token"), "channelId=" + channel + "&type=text&text=rollback").getIntValue("code"));
        assertEquals(2, jdbc.queryValue("select count(*) from im_channel_msg where channel_id = ?", Integer.class, Long.valueOf(channel)));
        assertEquals(0, request("GET", "getDialogueByChannelId?channelId=" + channel, b.getString("token"), null).getJSONObject("model").getIntValue("unreadCount"));
    }

    @Test
    void concurrentSendAndReadKeepNewMessagesUnread() throws Exception {
        JSONObject a = login("13800138000"), b = login("13800138001");
        String channel = request("POST", "createPrivateDialogue", a.getString("token"), "sid=" + b.getString("sid")).getJSONObject("model").getString("channelId");
        request("POST", "sendMsgToChannel", a.getString("token"), "channelId=" + channel + "&type=text&text=first");
        String first = request("GET", "getMsgListByChannelId?channelId=" + channel, b.getString("token"), null).getJSONArray("channelMsgList").getJSONObject(0).getString("id");
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch start = new CountDownLatch(1);
            Future<JSONObject> read = executor.submit(() -> { start.await(); return request("POST", "markRead", b.getString("token"), "channelId=" + channel + "&messageId=" + first); });
            Future<JSONObject> send = executor.submit(() -> { start.await(); return request("POST", "sendMsgToChannel", a.getString("token"), "channelId=" + channel + "&type=text&text=new"); });
            start.countDown();
            assertEquals(0, read.get(10, TimeUnit.SECONDS).getIntValue("code"));
            assertEquals(0, send.get(10, TimeUnit.SECONDS).getIntValue("code"));
            assertTrue(request("GET", "getDialogueByChannelId?channelId=" + channel, b.getString("token"), null).getJSONObject("model").getIntValue("unreadCount") > 0);
        } finally { executor.shutdownNow(); }
    }

    WebSocket connect(String path, Messages listener) {
        return client.newWebSocketBuilder().connectTimeout(Duration.ofSeconds(5))
                .buildAsync(URI.create("ws://127.0.0.1:" + ctx.getWebPort() + path), listener).join();
    }

    static class Messages implements WebSocket.Listener {
        final BlockingQueue<String> messages = new LinkedBlockingQueue<>();
        final StringBuilder partial = new StringBuilder();
        public void onOpen(WebSocket socket) { socket.request(1); }
        public CompletionStage<?> onText(WebSocket socket, CharSequence text, boolean last) {
            partial.append(text);
            if (last) {
                messages.add(partial.toString());
                partial.setLength(0);
            }
            socket.request(1);
            return null;
        }
        String next() throws InterruptedException { return messages.poll(5, TimeUnit.SECONDS); }
    }
}
