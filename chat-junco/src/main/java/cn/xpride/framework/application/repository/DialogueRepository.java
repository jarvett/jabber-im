package cn.xpride.framework.application.repository;

import cn.xpride.framework.application.entity.Dialogue;
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
public class DialogueRepository {
    private final JdbcTemplate jdbc;
    private static final RowMapper<Dialogue> ROW = rs -> {
        Dialogue item = new Dialogue();
        item.setId(rs.getObject("id", Long.class));
        item.setSubscriberId(rs.getObject("subscriber_id", Long.class));
        item.setChannelId(rs.getObject("channel_id", Long.class));
        item.setType(rs.getObject("type", Integer.class));
        item.setName(rs.getObject("name", String.class));
        item.setAvatar(rs.getObject("avatar", String.class));
        item.setBrief(rs.getObject("brief", String.class));
        item.setLastTime(rs.getObject("last_time", Long.class));
        item.setLastReadTime(rs.getObject("last_read_time", Long.class));
        item.setUnreadCount(rs.getObject("unread_count", Integer.class));
        item.setIsVisible(rs.getObject("is_visible", Integer.class));
        item.setShowOuter(rs.getObject("show_outer", Integer.class));
        return item;
    };

    public void save(Dialogue item) {
        jdbc.update("insert into im_dialogue (id, subscriber_id, channel_id, type, name, avatar, brief, last_time, last_read_time, unread_count, is_visible, show_outer) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                item.getId(), item.getSubscriberId(), item.getChannelId(), item.getType(), item.getName(), item.getAvatar(), item.getBrief(), item.getLastTime(), item.getLastReadTime(), item.getUnreadCount(), item.getIsVisible(), item.getShowOuter());
    }

    public Long getNewDialogueId() {
        long id = LongUtil.getInstance().nextId();
        while (countById(id) > 0) {
            id = LongUtil.getInstance().nextId();
        }
        return id;
    }

    public int countById(Long id) {
        return jdbc.queryValue("select count(*) from im_dialogue where id = ?", Integer.class, id);
    }

    public List<Dialogue> findPageBySubscriberIdAndShowOuter(Long subscriberId,Integer showOuter,Integer pageStart,Integer pageSize) {
        return jdbc.query("select * from  im_dialogue where subscriber_id = ? and show_outer = ? order by last_time desc limit ?,? ", ROW, subscriberId, showOuter, pageStart, pageSize);
    }

    public void updateHasRead(Long id,  Long now,  String brief) {
        jdbc.update("update im_dialogue set last_time = ?,last_read_time = ? ,unread_count=0,brief = ? where id = ?", now, now, brief, id);
    }

    public void updateUnread(Long id,  Long now,  String brief) {
        jdbc.update("update im_dialogue set last_time = ?,unread_count = unread_count + 1,brief = ? where id = ?", now, brief, id);
    }

    public void updateAvatarAndName(Long id,  String avatar,  String name) {
        jdbc.update("update im_dialogue set avatar = ?,name = ? where id = ?", avatar, name, id);
    }

    public Dialogue getBySubscriberIdAndChannelId(Long subscriberId, Long channelId) {
        return jdbc.queryOne("select * from im_dialogue where subscriber_id = ? and channel_id = ?", ROW, subscriberId, channelId);
    }

    public void markRead(Long id, Long readTime) {
        jdbc.update("update im_dialogue set unread_count = 0, last_read_time = greatest(last_read_time, ?) where id = ?", readTime, id);
    }
}
