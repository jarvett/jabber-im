package cn.xpride.framework.application.repository;

import cn.xpride.framework.application.entity.Channel;
import cn.xpride.framework.utility.LongUtil;
import io.github.jarvett.core.ioc.annotation.JuncoManaged;
import io.github.jarvett.jdbc.core.JdbcTemplate;
import io.github.jarvett.jdbc.core.RowMapper;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Collections;

/** 通过 Junco JDBC 执行原有查询，保持表字段、排序和分页语义。 */
@JuncoManaged
@RequiredArgsConstructor
public class ChannelRepository {
    private final JdbcTemplate jdbc;
    private static final RowMapper<Channel> ROW = rs -> {
        Channel item = new Channel();
        item.setId(rs.getObject("id", Long.class));
        item.setType(rs.getObject("type", Integer.class));
        item.setChannelConfig(rs.getObject("channel_config", String.class));
        item.setIsNeedAccess(rs.getObject("is_need_access", Integer.class));
        item.setJoinSubscriberIds(rs.getObject("join_subscriber_ids", String.class));
        item.setMsgCount(rs.getObject("msg_count", Integer.class));
        item.setCreateTime(rs.getObject("create_time", Long.class));
        item.setUpdateTime(rs.getObject("update_time", Long.class));
        return item;
    };

    public void save(Channel item) {
        jdbc.update("insert into im_channel (id, type, channel_config, is_need_access, join_subscriber_ids, msg_count, create_time, update_time) values (?, ?, ?, ?, ?, ?, ?, ?)",
                item.getId(), item.getType(), item.getChannelConfig(), item.getIsNeedAccess(), item.getJoinSubscriberIds(), item.getMsgCount(), item.getCreateTime(), item.getUpdateTime());
    }

    public Long getNewChannelId() {
        long id = LongUtil.getInstance().nextId();
        while (countById(id) > 0) {
            id = LongUtil.getInstance().nextId();
        }
        return id;
    }

    public int countById(Long id) {
        return jdbc.queryValue("select count(*) from im_channel where id = ?", Integer.class, id);
    }

    public void updateMsgCount(Long channelId, Long now) {
        jdbc.update("update im_channel set update_time = ?,msg_count = msg_count + 1 where id = ?", now, channelId);
    }

    public Channel findFirstById(Long id) {
        return jdbc.queryOne("select * from im_channel where id = ? limit 1", ROW, id);
    }

    public Channel getFirstByTypeAndJoinSubscriberIds(Integer type, String joinSubscriberIds) {
        return jdbc.queryOne("select * from im_channel where type = ? and join_subscriber_ids = ? limit 1", ROW, type, joinSubscriberIds);
    }

    // 与发送消息、已读确认共用频道行锁，保证未读计数和消息落库串行提交。
    public Channel lockById(Long id) {
        return jdbc.queryOne("select * from im_channel where id = ? for update", ROW, id);
    }
}
