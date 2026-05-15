package cn.xpride.framework.application.repository;

import cn.xpride.framework.application.entity.Subscriber;
import cn.xpride.framework.utility.LongUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber,Long> {

    default Long getNewSubscriberId() {
        long id = LongUtil.getInstance().nextId();
        while (countById(id) > 0) {
            id = LongUtil.getInstance().nextId();
        }
        return id;
    }

    int countById(Long id);

    Subscriber findFirstById(Long id);

    Subscriber findTopByAppIdAndUniqueId(String appId, String uniqueId);

    List<Subscriber> findAllByIdIn(List<Long> ids);

    @Query(value = "select * from im_subscriber where id != :notInId order by rand() limit 5",nativeQuery = true)
    List<Subscriber> randomSubscriber(@Param("notInId") Long notInId);

    @Transactional
    @Modifying
    @Query(value = "update im_subscriber set last_login_time = :now where id = :id",nativeQuery = true)
    void updateLaseLoginTimeById(@Param("id") Long id,@Param("now") Long now);

    @Transactional
    @Modifying
    @Query(value = "update im_subscriber set avatar = :avatarUrl,nickname = :nickName where id = :id",nativeQuery = true)
    void updateAvatarAndNickName(@Param("id") Long id, @Param("avatarUrl")String avatarUrl, @Param("nickName")String nickName);

    @Transactional
    @Modifying
    @Query(value = "update im_subscriber set intro = :intro where id = :id",nativeQuery = true)
    void updateIntro(@Param("id") Long userId, @Param("intro") String intro);

}
