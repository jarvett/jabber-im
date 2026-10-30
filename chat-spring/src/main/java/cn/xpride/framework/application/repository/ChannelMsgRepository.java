package cn.xpride.framework.application.repository;

import cn.xpride.framework.application.entity.ChannelMsg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelMsgRepository extends JpaRepository<ChannelMsg,Long> {

    @Query(value = "select * from im_channel_msg where channel_id = :channelId order by time desc, length(id) desc, id desc limit :pageStart,:pageSize",nativeQuery = true)
    List<ChannelMsg> findPageByChannelId(@Param("channelId") Long channelId, @Param("pageStart")Integer pageStart, @Param("pageSize") Integer pageSize);

    ChannelMsg findFirstById(String id);
}
