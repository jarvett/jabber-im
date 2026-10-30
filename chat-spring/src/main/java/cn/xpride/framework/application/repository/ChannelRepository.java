package cn.xpride.framework.application.repository;

import cn.xpride.framework.application.entity.Channel;
import cn.xpride.framework.utility.LongUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel,Long> {

    default Long getNewChannelId() {
        long id = LongUtil.getInstance().nextId();
        while (countById(id) > 0) {
            id = LongUtil.getInstance().nextId();
        }
        return id;
    }

    int countById(Long id);

    Channel findFirstById(Long id);

    Channel getFirstByTypeAndJoinSubscriberIds(Integer type,String joinSubscriberIds);

    @Transactional
    @Modifying
    @Query(value = "update im_channel set update_time = :now,msg_count = msg_count + 1 where id = :channelId",nativeQuery = true)
    void updateMsgCount(@Param("channelId") Long channelId,@Param("now") Long now);

    // 与发送消息、已读确认共用频道行锁，保证未读计数和消息落库串行提交。
    @Query(value = "select * from im_channel where id = :id for update", nativeQuery = true)
    Channel lockById(@Param("id") Long id);
}
