package cn.xpride.framework.application.repository;

import cn.xpride.framework.application.entity.ChannelMsg;
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
public class ChannelMsgRepository {
    private final JdbcTemplate jdbc;
    private static final RowMapper<ChannelMsg> ROW = rs -> {
        ChannelMsg item = new ChannelMsg();
        item.setId(rs.getObject("id", String.class));
        item.setSubscriberId(rs.getObject("subscriber_id", Long.class));
        item.setChannelId(rs.getObject("channel_id", Long.class));
        item.setContent(rs.getObject("content", String.class));
        item.setTime(rs.getObject("time", Long.class));
        return item;
    };

    public void save(ChannelMsg item) {
        jdbc.update("insert into im_channel_msg (id, subscriber_id, channel_id, content, time) values (?, ?, ?, ?, ?)",
                item.getId(), item.getSubscriberId(), item.getChannelId(), item.getContent(), item.getTime());
    }

    public List<ChannelMsg> findPageByChannelId(Long channelId, Integer pageStart,  Integer pageSize) {
        return jdbc.query("select * from im_channel_msg where channel_id = ? order by time desc, length(id) desc, id desc limit ?,?", ROW, channelId, pageStart, pageSize);
    }

    public ChannelMsg findFirstById(String id) {
        return jdbc.queryOne("select * from im_channel_msg where id = ?", ROW, id);
    }
}
