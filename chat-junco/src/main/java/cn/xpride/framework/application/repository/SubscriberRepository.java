package cn.xpride.framework.application.repository;

import cn.xpride.framework.application.entity.Subscriber;
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
public class SubscriberRepository {
    private final JdbcTemplate jdbc;
    private static final RowMapper<Subscriber> ROW = rs -> {
        Subscriber item = new Subscriber();
        item.setId(rs.getObject("id", Long.class));
        item.setType(rs.getObject("type", Integer.class));
        item.setAppId(rs.getObject("app_id", String.class));
        item.setUniqueId(rs.getObject("unique_id", String.class));
        item.setNickname(rs.getObject("nickname", String.class));
        item.setAvatar(rs.getObject("avatar", String.class));
        item.setIntro(rs.getObject("intro", String.class));
        item.setCreateTime(rs.getObject("create_time", Long.class));
        item.setUpdateTime(rs.getObject("update_time", Long.class));
        item.setLastLoginTime(rs.getObject("last_login_time", Long.class));
        return item;
    };

    public void save(Subscriber item) {
        jdbc.update("insert into im_subscriber (id, type, app_id, unique_id, nickname, avatar, intro, create_time, update_time, last_login_time) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                item.getId(), item.getType(), item.getAppId(), item.getUniqueId(), item.getNickname(), item.getAvatar(), item.getIntro(), item.getCreateTime(), item.getUpdateTime(), item.getLastLoginTime());
    }

    public Long getNewSubscriberId() {
        long id = LongUtil.getInstance().nextId();
        while (countById(id) > 0) {
            id = LongUtil.getInstance().nextId();
        }
        return id;
    }

    public int countById(Long id) {
        return jdbc.queryValue("select count(*) from im_subscriber where id = ?", Integer.class, id);
    }

    public List<Subscriber> randomSubscriber(Long notInId) {
        return jdbc.query("select * from im_subscriber where id != ? order by rand() limit 5", ROW, notInId);
    }

    public void updateLaseLoginTimeById(Long id, Long now) {
        jdbc.update("update im_subscriber set last_login_time = ? where id = ?", now, id);
    }

    public void updateAvatarAndNickName(Long id, String avatarUrl, String nickName) {
        jdbc.update("update im_subscriber set avatar = ?,nickname = ? where id = ?", avatarUrl, nickName, id);
    }

    public void updateIntro(Long userId,  String intro) {
        jdbc.update("update im_subscriber set intro = ? where id = ?", intro, userId);
    }

    public Subscriber findFirstById(Long id) {
        return jdbc.queryOne("select * from im_subscriber where id = ? limit 1", ROW, id);
    }

    public Subscriber findTopByAppIdAndUniqueId(String appId, String uniqueId) {
        return jdbc.queryOne("select * from im_subscriber where app_id = ? and unique_id = ? limit 1", ROW, appId, uniqueId);
    }

    public List<Subscriber> findAllByIdIn(List<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        return jdbc.query("select * from im_subscriber where id in (" + placeholders + ")", ROW, ids.toArray());
    }
}
