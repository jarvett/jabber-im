package cn.xpride.framework.application.repository;

import cn.xpride.framework.application.entity.Dialogue;
import cn.xpride.framework.utility.LongUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface DialogueRepository extends JpaRepository<Dialogue,Long> {

    default Long getNewDialogueId() {
        long id = LongUtil.getInstance().nextId();
        while (countById(id) > 0) {
            id = LongUtil.getInstance().nextId();
        }
        return id;
    }

    int countById(Long id);

    Dialogue getBySubscriberIdAndChannelId(Long subscriberId,Long channelId);

    @Query(value = "select * from  im_dialogue where subscriber_id = :subscriberId and show_outer = :showOuter order by last_time desc limit :pageStart,:pageSize ",nativeQuery = true)
    List<Dialogue> findPageBySubscriberIdAndShowOuter(@Param("subscriberId")Long subscriberId,@Param("showOuter")Integer showOuter,@Param("pageStart")Integer pageStart,@Param("pageSize")Integer pageSize);

    @Transactional
    @Modifying
    @Query(value = "update im_dialogue set last_time = :now,last_read_time = :now ,unread_count=0,brief = :brief where id = :id",nativeQuery = true)
    void updateHasRead(@Param("id") Long id,@Param("now")  Long now,@Param("brief")  String brief);

    @Transactional
    @Modifying
    @Query(value = "update im_dialogue set last_time = :now,unread_count = unread_count + 1,brief = :brief where id = :id",nativeQuery = true)
    void updateUnread(@Param("id") Long id,@Param("now")  Long now,@Param("brief")  String brief);

    @Transactional
    @Modifying
    @Query(value = "update im_dialogue set avatar = :avatar,name = :name where id = :id",nativeQuery = true)
    void updateAvatarAndName(@Param("id") Long id,@Param("avatar")  String avatar,@Param("name")  String name);

    @Modifying
    @Query(value = "update im_dialogue set unread_count = 0, last_read_time = greatest(last_read_time, :readTime) where id = :id", nativeQuery = true)
    void markRead(@Param("id") Long id, @Param("readTime") Long readTime);
}
